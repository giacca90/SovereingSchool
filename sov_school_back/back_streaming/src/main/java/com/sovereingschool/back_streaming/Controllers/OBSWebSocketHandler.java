package com.sovereingschool.back_streaming.Controllers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sovereingschool.back_streaming.Services.StreamingService;

public class OBSWebSocketHandler extends TextWebSocketHandler {
    private final String RTMP_URL = "rtmp://localhost:8060/live/";
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Thread> ffmpegThreads = new ConcurrentHashMap<>();
    private final Map<String, Thread> previews = new ConcurrentHashMap<>();
    private final StreamingService streamingService;
    private final Executor executor;

    public OBSWebSocketHandler(Executor executor, StreamingService streamingService) {
        this.streamingService = streamingService;
        this.executor = executor;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        Boolean isAuthenticate = (boolean) session.getAttributes().get("Authenticate");
        if (isAuthenticate == null || !isAuthenticate) {
            String error = (String) session.getAttributes().get("Error");
            System.out.println("Falló la autenticación en WebRTC: " + error);
            try {
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"" + error + "\"}"));
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            } catch (IOException e) {
                System.err.println("Error al enviar mensaje de error en OBS: " + e.getMessage());
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    System.err.println("Error al cerrar sesión WebSocket: " + ex.getMessage());
                }
            }
            return;
        }

        Authentication auth = (Authentication) session.getAttributes().get("user");
        if (!isAuthorized(auth)) {
            System.err.println("Acceso denegado: usuario no autorizado");
            try {
                session.sendMessage(new TextMessage(
                        "{\"type\":\"error\",\"message\":\"" + "Acceso denegado: usuario no autorizado" + "\"}"));
                session.close(CloseStatus.POLICY_VIOLATION);
            } catch (IOException e) {
                System.err.println("Error al enviar mensaje de error en OBS: " + e.getMessage());
                try {
                    session.close(CloseStatus.SERVER_ERROR);
                } catch (IOException ex) {
                    System.err.println("Error al cerrar sesión WebSocket: " + ex.getMessage());
                }
            }
            return;
        }

        sessions.put(session.getId(), session);
        String username = (String) session.getAttributes().get("username");
        System.out.println("Conexión establecida en OBS para el usuario: " + username);
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = session.getId();
        streamingService.stopFFmpegProcessForUser(userId);
        sessions.remove(userId);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        // Parsear el mensaje recibido
        String payload = message.getPayload();

        if (payload.contains("request_rtmp_url")) {
            // Extraer userId
            String userId = extractUserId(payload);

            if (userId != null) {
                // Generar URL RTMP para OBS
                String rtmpUrl = RTMP_URL + userId + "_" + session.getId();

                // Preparar la previsualización de la transmisión
                executor.execute(() -> {
                    Thread currentThread = Thread.currentThread();
                    previews.put(session.getId(), currentThread); // Añadir el hilo al mapa
                    try {
                        this.streamingService.startPreview(rtmpUrl);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                // Enviar la URL generada al cliente
                session.sendMessage(new TextMessage("{\"type\":\"rtmp_url\",\"rtmpUrl\":\"" + rtmpUrl + "\"}"));
            } else {
                // Enviar error si no se encuentra el userId
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"userId no proporcionado\"}"));
            }
        } else if (payload.contains("emitirOBS") && payload.contains("rtmpUrl")) {
            String streamId = this.extractStreamId(payload);
            this.startFFmpegProcessForUser(streamId, RTMP_URL + streamId);
        } else if (payload.contains("detenerStreamOBS")) {
            this.streamingService.stopFFmpegProcessForUser(this.extractStreamId(payload));
        } else {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Tipo de mensaje no reconocido\"}"));
        }
    }

    private void startFFmpegProcessForUser(String userId, String rtmpUrl) {
        if (ffmpegThreads.containsKey(userId)) {
            System.err.println("El proceso FFmpeg ya está corriendo para el usuario " + userId);
            return;
        }

        // Usar el Executor para ejecutar el proceso FFmpeg en un hilo separado
        executor.execute(() -> {
            Thread currentThread = Thread.currentThread();
            ffmpegThreads.put(userId.substring(userId.lastIndexOf("_") + 1), currentThread); // Añadir el hilo al mapa
            try {
                String[] streamIdAndSettings = { userId, null, null, null };
                this.streamingService.startLiveStreamingFromStream(streamIdAndSettings, rtmpUrl, null);
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
        // {"event":"emitirOBS","rtmpUrl":"rtmp://localhost:8060/live/1_31973234-fb5c-4140-a9f1-00cac84f3b60"}
        if (payload.contains("rtmpUrl")) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode;
            try {
                jsonNode = objectMapper.readTree(payload);
                String streamURL = jsonNode.get("rtmpUrl").asText();
                return streamURL.substring(streamURL.lastIndexOf("/") + 1);
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private boolean isAuthorized(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_PROF") || role.equals("ROLE_ADMIN"));
    }
}