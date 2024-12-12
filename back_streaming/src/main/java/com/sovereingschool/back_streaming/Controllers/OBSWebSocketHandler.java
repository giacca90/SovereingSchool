package com.sovereingschool.back_streaming.Controllers;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
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
        sessions.remove(userId);

        Thread ffmpegThread = ffmpegThreads.remove(userId);
        if (ffmpegThread != null && ffmpegThread.isAlive()) {
            ffmpegThread.join();
        }

        Thread previewThread = previews.remove(userId);
        if (previewThread != null && previewThread.isAlive()) {
            previewThread.join();
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        // Parsear el mensaje recibido
        String payload = message.getPayload();
        System.out.println("Mensaje recibido en OBS handler: " + payload);

        // Suponiendo que el mensaje incluye un userId
        // Puedes usar una biblioteca como Jackson para manejar JSON si es necesario
        if (payload.contains("request_rtmp_url")) {
            // Extraer userId (puedes usar un parser real en producción)
            String userId = extractUserId(payload);

            if (userId != null) {
                // Generar URL RTMP para OBS
                String randomID = UUID.randomUUID().toString();
                String rtmpUrl = RTMS_URL + userId + "_" + randomID;

                // Preparar la previsualización de la transmisión
                executor.execute(() -> {
                    try {
                        this.streamingService.startPreview(rtmpUrl);
                        Thread currentThread = Thread.currentThread();
                        previews.put(userId, currentThread); // Añadir el hilo al mapa
                    } catch (IOException e) {
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

        } else if (payload.contains("detenerStreamOBS") && payload.contains("rtmpUrl")) {
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
            try {
                this.streamingService.startLiveStreamingFromStream(userId, rtmpUrl);
                // Registrar el hilo en el mapa después de iniciar el proceso FFmpeg
                Thread currentThread = Thread.currentThread();
                ffmpegThreads.put(userId, currentThread); // Añadir el hilo al mapa
            } catch (IOException e) {
                System.err.println("Error al iniciar FFmpeg para usuario " + userId + ": " + e.getMessage());
            }
        });
    }

    private String extractUserId(String payload) {
        // Implementar lógica para extraer el userId del mensaje
        // Ejemplo simplista: Si el payload es JSON:
        // {"type":"request_rtmp_url","userId":"123"}
        if (payload.contains("userId")) {
            return payload.replaceAll("[^0-9]", ""); // Extraer números como un ejemplo simple
        }
        return null;
    }

    private String extractStreamId(String payload) {
        // Implementar lógica para extraer el streamId del mensaje
        // Ejemplo simplista: Si el payload es JSON:
        // {"event":"emitirOBS","rtmpUrl":"rtmp://localhost:8060/live/1_31973234-fb5c-4140-a9f1-00cac84f3b60"}
        if (payload.contains("rtmpUrl")) {
            String streamId = payload.substring(payload.lastIndexOf("/") + 1, payload.length() - 2);
            System.out.println("Stream ID: " + streamId);
            return streamId;
        }
        return null;
    }

}