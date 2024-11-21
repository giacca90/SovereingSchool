package com.sovereingschool.back_base.Configurations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class WebRTCSignalingHandler extends TextWebSocketHandler {

    // Almacena las sesiones activas
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Agregar la nueva sesión
        sessions.put(session.getId(), session);
        System.out.println("Nueva conexión: " + session.getId());
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Lógica para manejar mensajes de señalización
        String payload = message.getPayload();
        System.out.println("Mensaje recibido de " + session.getId() + ": " + payload);

        // Reenviar el mensaje a todos los clientes conectados, excepto al remitente
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            if (!entry.getKey().equals(session.getId())) {
                entry.getValue().sendMessage(new TextMessage(payload));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status)
            throws Exception {
        // Eliminar la sesión cerrada
        sessions.remove(session.getId());
        System.out.println("Conexión cerrada: " + session.getId());
    }
}