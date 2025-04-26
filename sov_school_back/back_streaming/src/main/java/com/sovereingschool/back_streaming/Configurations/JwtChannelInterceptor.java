package com.sovereingschool.back_streaming.Configurations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import com.sovereingschool.back_common.Utils.JwtUtil;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate; // para enviar errores al cliente

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(message);

        // Solo interesan mensajes que vayan al broker (SEND, SUBSCRIBE, etc.)
        if (StompCommand.SEND.equals(sha.getCommand()) ||
                StompCommand.SUBSCRIBE.equals(sha.getCommand())) {

            String authHeader = sha.getFirstNativeHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                sendError(sha, "No se envió token de autenticación");
                return null; // descarta este mensaje, no cierra sesión
            }
            String token = authHeader.substring(7);

            try {
                jwtUtil.createAuthenticationFromToken(token);
                // token válido → deja pasar el mensaje
                return message;
            } catch (AuthenticationException ex) {
                sendError(sha, "Token inválido o expirado");
                return null; // descarta el mensaje, sin cerrar
            }
        }
        // Para otros comandos, dejar pasar
        return message;
    }

    private void sendError(StompHeaderAccessor sha, String errorMsg) {
        String simpSessionId = sha.getSessionId();
        if (simpSessionId == null) {
            throw new IllegalArgumentException("Session ID cannot be null");
        }
        // Enviar al usuario un error en la cola /queue/errors-user-{sessionId}
        messagingTemplate.convertAndSendToUser(
                simpSessionId,
                "/queue/errors",
                errorMsg);
    }
}
