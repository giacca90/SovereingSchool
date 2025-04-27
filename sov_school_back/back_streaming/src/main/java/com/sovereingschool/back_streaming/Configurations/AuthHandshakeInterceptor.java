package com.sovereingschool.back_streaming.Configurations;

import java.net.URI;
import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.sovereingschool.back_common.Utils.JwtUtil;

@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil; // Tu servicio para validar tokens

    public AuthHandshakeInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        URI uri = request.getURI();
        String query = uri.getQuery(); // token=xxxxx
        if (query == null || !query.contains("token=")) {
            System.out.println("Falta el token Bearer en la URL");
            attributes.put("Authenticate", false);
            attributes.put("Error", "Falta el token Bearer en la URL");
            return true;
        }

        String token = query.replaceFirst(".*token=", "").split("&")[0];

        try {
            jwtUtil.isTokenValid(token);
        } catch (AuthenticationException e) {
            System.out.println("Token inválido en el HandShake de streaming: " + e.getMessage());
            attributes.put("Authenticate", false);
            attributes.put("Error", "Token inválido en el HandShake de streaming: " + e.getMessage());
            return true;
        }

        // Opcional: puedes guardar el usuario en attributes para luego usarlo en el
        // WebSocketHandler
        String username = jwtUtil.getUsername(token);
        attributes.put("username", username);
        attributes.put("Authenticate", true);
        attributes.put("user", jwtUtil.createAuthenticationFromToken(token));
        return true; // Aceptar conexión
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @Nullable Exception exception) {
        // No necesitamos hacer nada aquí
    }
}
