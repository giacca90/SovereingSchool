package com.sovereingschool.back_streaming.Controllers;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.sovereingschool.back_streaming.Services.StreamingService;

public class OBSWebSocketHandler extends TextWebSocketHandler {
    private final String RTMS_URL = "rtmp://localhost:8060/live/";
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
        sessions.put(session.getId(), session);
        System.out.println("Nueva conexión: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String userId = session.getId();
        System.out.println("Se cierra la conexión " + userId);
        sessions.remove(userId);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        // Parsear el mensaje recibido
        String payload = message.getPayload();
        System.out.println("Mensaje recibido en OBS handler: " + payload);

        // Suponiendo que el mensaje incluye un userId
        if (payload.contains("request_rtmp_url")) {
            // Extraer userId (puedes usar un parser real en producción)
            String userId = extractUserId(payload);

            if (userId != null) {
                // Generar URL RTMP para OBS
                String rtmpUrl = RTMS_URL + userId + "_" + session.getId();

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
            this.startFFmpegProcessForUser(this.extractStreamId(payload), RTMS_URL + this.extractStreamId(payload));

        } else if (payload.contains("detenerStreamOBS")) {
            this.streamingService.stopFFmpegProcessForUser(this.extractStreamId(payload));
        } else {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Tipo de mensaje no reconocido\"}"));
        }
    }

    private void startFFmpegProcessForUser(String userId, String rtmpUrl) {
        if (ffmpegThreads.containsKey(userId)) {
            System.out.println("El proceso FFmpeg ya está corriendo para el usuario " + userId);
            return;
        }

        // Usar el Executor para ejecutar el proceso FFmpeg en un hilo separado
        executor.execute(() -> {
            Thread currentThread = Thread.currentThread();
            ffmpegThreads.put(userId.substring(userId.lastIndexOf("_") + 1), currentThread); // Añadir el hilo al mapa
            try {
                this.streamingService.startLiveStreamingFromStream(userId, rtmpUrl);
            } catch (IOException e) {
                System.err.println("Error al iniciar FFmpeg para usuario " + userId + ": " + e.getMessage());
            }
        });
    }

    private String extractUserId(String payload) {
        // {"type":"request_rtmp_url","userId":"123"}
        if (payload.contains("userId")) {
            return payload.replaceAll("[^0-9]", ""); // Extraer números como un ejemplo simple
        }
        return null;
    }

    private String extractStreamId(String payload) {
        // {"event":"emitirOBS","rtmpUrl":"rtmp://localhost:8060/live/1_31973234-fb5c-4140-a9f1-00cac84f3b60"}
        if (payload.contains("rtmpUrl")) {
            String streamId = payload.substring(payload.lastIndexOf("/") + 1, payload.length() - 2);
            return streamId;
        }
        return null;
    }

}