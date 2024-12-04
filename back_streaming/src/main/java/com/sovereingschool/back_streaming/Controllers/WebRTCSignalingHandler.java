package com.sovereingschool.back_streaming.Controllers;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import com.sovereingschool.back_streaming.Models.UserStreams;
import com.sovereingschool.back_streaming.Services.StreamingService;

public class WebRTCSignalingHandler extends BinaryWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, UserStreams> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Thread> ffmpegThreads = new ConcurrentHashMap<>();
    private final Executor executor; // Executor inyectado

    @Autowired
    private StreamingService streamingService;

    // Constructor modificado para aceptar Executor
    public WebRTCSignalingHandler(Executor executor) {
        this.executor = executor;
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
                userStreams.getOutputStream().close();
                userStreams.getInputStream().close();
            }

            Thread ffmpegThread = ffmpegThreads.remove(userId);
            if (ffmpegThread != null && ffmpegThread.isAlive()) {
                ffmpegThread.join();
            }
        } catch (Exception e) {
            System.err.println("Error al cerrar recursos para el usuario: " + e.getMessage());
        }

        System.out.println("Conexión cerrada: " + userId + " Razón: " + status.getReason());
    }

    @Override
    protected void handleBinaryMessage(@NonNull WebSocketSession session, @NonNull BinaryMessage message) {
        byte[] payload = message.getPayload().array();
        System.out.println("Mensaje recibido desde WebSocket, tamaño de payload: " + payload.length);

        UserStreams userStreams = userSessions.computeIfAbsent(session.getId(), key -> {
            try {
                PipedInputStream userInputStream = new PipedInputStream();
                PipedOutputStream userOutputStream = new PipedOutputStream(userInputStream);
                startFFmpegProcessForUser(session.getId(), userInputStream);
                return new UserStreams(userInputStream, userOutputStream);
            } catch (IOException e) {
                e.printStackTrace();
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
            userStreams.getOutputStream().write(payload);
            userStreams.getOutputStream().flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException ex) {
                System.err.println("Error al cerrar sesión WebSocket: " + ex.getMessage());
            }
        }
    }

    private void startFFmpegProcessForUser(String userId, PipedInputStream userInputStream) {
        if (ffmpegThreads.containsKey(userId)) {
            System.out.println("El proceso FFmpeg ya está corriendo para el usuario " + userId);
            return;
        }

        // Usar el Executor para ejecutar el proceso FFmpeg en un hilo separado
        executor.execute(() -> {
            try {
                this.streamingService.startLiveStreamingFromStream(userId, userInputStream);
            } catch (IOException e) {
                System.err.println("Error al iniciar FFmpeg para usuario " + userId + ": " + e.getMessage());
            }
        });
    }
}
