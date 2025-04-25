package com.sovereingschool.back_chat.Configurations;

import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        Authentication authentication = null;

        if (accessor.getCommand() == StompCommand.CONNECT) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
            if (sessionAttributes != null) {
                Object rawAuth = sessionAttributes.get("user");
                if (rawAuth instanceof Authentication auth) {
                    authentication = auth;
                    accessor.setUser(auth);
                }
            }
        } else {
            // Para todos los mensajes posteriores, sacamos el user ya seteado en el
            // accessor
            if (accessor.getUser() instanceof Authentication auth) {
                authentication = auth;
            }
        }

        if (authentication != null) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            System.err.println("No hay autenticación en el WebSocket");
        }

        return message;
    }

    @Override
    public void afterSendCompletion(@NonNull Message<?> message, @NonNull MessageChannel channel, boolean sent,
            @Nullable Exception ex) {
        // Limpia el contexto para evitar fugas de seguridad entre hilos
        SecurityContextHolder.clearContext();
    }
}
