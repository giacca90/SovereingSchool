package com.sovereingschool.back_streaming.Controllers;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.sovereingschool.back_streaming.Services.StreamingService;

public class OBSWebSocketHandler extends TextWebSocketHandler {
    private final StreamingService streamingService;

    public OBSWebSocketHandler(StreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String rtmpUrl = "";
        String randomID = UUID.randomUUID().toString();
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
                rtmpUrl = "rtmp://localhost:8060/live/" + userId + "_" + randomID;

                // Enviar la URL generada al cliente
                session.sendMessage(new TextMessage("{\"type\":\"rtmp_url\",\"rtmpUrl\":\"" + rtmpUrl + "\"}"));
            } else {
                // Enviar error si no se encuentra el userId
                session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"userId no proporcionado\"}"));
            }
        } else if (payload.contains("emitirOBS") && payload.contains("rtmpUrl")) {
            this.streamingService.startLiveStreamingFromStream(this.extractStreamId(payload), rtmpUrl);

        } else {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"Tipo de mensaje no reconocido\"}"));
        }
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