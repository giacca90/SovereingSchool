package com.sovereingschool.back_chat.Configurations;

import java.util.Collection;
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
import org.springframework.security.core.GrantedAuthority;
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

        if (accessor.getCommand() == StompCommand.CONNECT) {
            Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
            String token = (sessionAttrs != null)
                    ? (String) sessionAttrs.get("token")
                    : null;

            if (token == null || token.isEmpty()) {
                SecurityContextHolder.clearContext();
                System.err.println("Falta token en sessionAttributes");
                throw new MessagingException("Falta token en sessionAttributes");
            }
            Long idUsuario = jwtUtil.getIdUsuario(token);
            try {
                Authentication auth = jwtUtil.createAuthenticationFromToken(token);
                // Creamos un token donde el name() es el ID:
                Authentication wsAuth = new Authentication() {
                    @Override
                    public Collection<? extends GrantedAuthority> getAuthorities() {
                        return auth.getAuthorities();
                    }

                    @Override
                    public Object getCredentials() {
                        return auth.getCredentials();
                    }

                    @Override
                    public Object getDetails() {
                        return idUsuario;
                    }

                    @Override
                    public Object getPrincipal() {
                        return auth.getPrincipal();
                    }

                    @Override
                    public boolean isAuthenticated() {
                        return auth.isAuthenticated();
                    }

                    @Override
                    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                        auth.setAuthenticated(isAuthenticated);
                    }

                    @Override
                    public String getName() {
                        return idUsuario.toString();
                    }
                };
                SecurityContextHolder.getContext().setAuthentication(wsAuth);
                accessor.setUser(wsAuth);
                return message;
            } catch (AuthenticationException ex) {
                SecurityContextHolder.clearContext();
                System.err.println("Token inválido: " + ex.getMessage());
                String destination = accessor.getDestination(); // Ej: "/app/init"
                if (destination != null) {
                    messagingTemplate.convertAndSendToUser(idUsuario.toString(), destination,
                            "Token inválido: " + ex.getMessage());
                    return null;
                } else {
                    System.err.println("No hay destino para el mensaje de refresh");
                    // Cerrar la conexión
                    throw new MessagingException("Token inválido y sin destino para enviar el mensaje");
                }
            }
        } else {
            // Para todos los mensajes posteriores, sacamos el user ya seteado en el
            // accessor
            if (accessor.getUser() instanceof Authentication auth) {
                SecurityContextHolder.getContext().setAuthentication(auth);
                return message;
            }
            throw new MessagingException("No hay autenticación en el WebSocket");
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
