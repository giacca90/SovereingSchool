package com.sovereingschool.back_chat.Configurations;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.sovereingschool.back_common.Utils.JwtUtil;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        Authentication authentication = null;

        if (accessor.getCommand() == StompCommand.CONNECT) {
            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
            String token = (sessionAttrs != null)
                    ? (String) sessionAttrs.get("token")
                    : null;

            if (token == null || token.isEmpty()) {
                SecurityContextHolder.clearContext();
                throw new MessagingException("Falta token en sessionAttributes");
            }
            try {
                Authentication auth = jwtUtil.createAuthenticationFromToken(token);
                accessor.setUser(auth);
                SecurityContextHolder.getContext().setAuthentication(auth);
                return message;
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                throw new MessagingException("Invalid token: " + ex.getMessage());
            }
        } else {
            // Para todos los mensajes posteriores, sacamos el user ya seteado en el
            // accessor
            if (accessor.getUser() instanceof Authentication auth) {
                authentication = auth;
            }
        }

        if (authentication != null) {
            String token = (String) authentication.getCredentials();
            try {
                jwtUtil.createAuthenticationFromToken(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return message;
            } catch (AuthenticationException e) {
                String destination = accessor.getDestination(); // Ej: "/app/init"
                if (destination != null) {
                    messagingTemplate.convertAndSend(destination, "Error en el token: " + e.getMessage());
                } else {
                    System.err.println("No hay destino para el mensaje de refresh");
                    // Cerrar la conexión
                    throw new MessagingException("Token inválido y sin destino para enviar el mensaje");
                }
                return null;
            }
        } else {
            System.err.println("No hay autenticación en el WebSocket");
            return null;
        }

    }

    @Override
    public void afterSendCompletion(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent,
            @Nullable Exception ex) {
        if (ex != null) {
            System.err.println("WebSocketAuthInterceptor: " + ex.getMessage());
        }
        // Limpia el contexto para evitar fugas de seguridad entre hilos
        SecurityContextHolder.clearContext();
    }
}
