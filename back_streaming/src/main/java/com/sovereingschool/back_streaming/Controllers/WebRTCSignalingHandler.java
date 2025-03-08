package com.sovereingschool.back_streaming.Controllers;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sovereingschool.back_streaming.Models.UserStreams;
import com.sovereingschool.back_streaming.Services.StreamingService;

public class WebRTCSignalingHandler extends BinaryWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, UserStreams> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Thread> ffmpegThreads = new ConcurrentHashMap<>();
    private final Map<String, String> sessionIdToStreamId = new ConcurrentHashMap<>();
    private final Executor executor; // Executor inyectado
    private final StreamingService streamingService;

    // Constructor modificado para aceptar Executor y StreamingService
    public WebRTCSignalingHandler(Executor executor, StreamingService streamingService) {
        this.executor = executor;
        this.streamingService = streamingService;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("Nueva conexión: " + session.getId());

    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        String userId = session.getId();
        sessions.remove(userId);

        try {
            UserStreams userStreams = userSessions.remove(userId);
            if (userStreams != null) {
                userStreams.getFFprobeOutputStream().close();
                userStreams.getFFprobeInputStream().close();
                userStreams.getFFmpegOutputStream().close();
                userStreams.getFFmpegInputStream().close();
            }
            this.streamingService.stopFFmpegProcessForUser(userId);

        } catch (Exception e) {
            System.err.println("Error al cerrar recursos para el usuario: " + e.getMessage());
        }

        System.out.println("Conexión cerrada: " + userId + " Razón: " + status.getReason());
    }

    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        // Parsear el mensaje recibido
        String payload = message.getPayload();
        System.out.println("Mensaje recibido en WebRTC handler: " + payload);

        if (payload.contains("userId")) {
            // Extraer userId
            String userId = extractUserId(payload);
            System.out.println("userId: " + userId);
            // Generar streamId
            String streamId = userId + "_" + session.getId();
            this.sessionIdToStreamId.put(session.getId(), streamId);
            try {
                session.sendMessage(new TextMessage("{\"type\":\"streamId\",\"streamId\":\"" + streamId + "\"}"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (payload.contains("detenerStreamWebcam")) {
            String streamId = this.extractStreamId(payload);
            try {
                this.streamingService.stopFFmpegProcessForUser(streamId);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) {
        byte[] payload = message.getPayload().array();
        System.out.println("Mensaje recibido desde WebSocket, tamaño de payload: " + payload.length);

        UserStreams userStreams = userSessions.computeIfAbsent(session.getId(), key -> {
            try {
                PipedInputStream ffprobeInputStream = new PipedInputStream(1024 * 1024);
                PipedOutputStream ffprobeOutputStream = new PipedOutputStream(ffprobeInputStream);
                PipedInputStream ffmpegInputStream = new PipedInputStream(1024 * 1024);
                PipedOutputStream ffmpegOutputStream = new PipedOutputStream(ffmpegInputStream);
                startFFmpegProcessForUser(this.sessionIdToStreamId.remove(session.getId()), ffprobeInputStream,
                        ffmpegInputStream);
                return new UserStreams(ffprobeInputStream, ffprobeOutputStream, ffmpegInputStream, ffmpegOutputStream);
            } catch (IOException e) {
                System.err.println("Error al iniciar FFmpeg para usuario " + session.getId() + ": " + e.getMessage());
                return null;
            }
        });

        if (userStreams == null) {
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException e) {
                System.err.println("Error al cerrar sesión WebSocket: " + e.getMessage());
            }
            return;
        }

        try {
            // Escribir en el flujo de FFprobe
            try {
                userStreams.getFFprobeOutputStream().write(payload.clone());
                userStreams.getFFprobeOutputStream().flush();
            } catch (IOException e) {
            }

            // Escribir en el flujo de FFmpeg
            try {
                userStreams.getFFmpegOutputStream().write(payload.clone());
                userStreams.getFFmpegOutputStream().flush();
            } catch (IOException e) {
            }
        } catch (Exception e) {
            System.err.println("Error general al escribir en los flujos: " + e.getMessage());
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ex) {
                System.err.println("Error al cerrar sesión WebSocket: " + ex.getMessage());
            }
        }
    }

    private void startFFmpegProcessForUser(String userId, PipedInputStream ffprobeInputStream,
            PipedInputStream ffmpegInputStream) {
        if (ffmpegThreads.containsKey(userId)) {
            System.out.println("El proceso FFmpeg ya está corriendo para el usuario " + userId);
            return;
        }

        // Usar el Executor para ejecutar el proceso FFmpeg en un hilo separado
        executor.execute(() -> {
            try {
                this.streamingService.startLiveStreamingFromStream(userId, ffprobeInputStream, ffmpegInputStream);
                // Registrar el hilo en el mapa después de iniciar el proceso FFmpeg
                Thread currentThread = Thread.currentThread();
                ffmpegThreads.put(userId, currentThread); // Añadir el hilo al mapa

            } catch (IOException e) {
                System.err.println("Error al iniciar FFmpeg para usuario " + userId + ": " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error al iniciar FFmpeg para usuario " + userId + ": " + e.getMessage());
            }
        });
    }

    private String extractUserId(String payload) {
        // {"type":"request_rtmp_url","userId":"123"}
        if (payload.contains("userId")) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(payload);
                return jsonNode.get("userId").asText();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String extractStreamId(String payload) {
        if (payload.contains("streamId")) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(payload);
                return jsonNode.get("streamId").asText();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
