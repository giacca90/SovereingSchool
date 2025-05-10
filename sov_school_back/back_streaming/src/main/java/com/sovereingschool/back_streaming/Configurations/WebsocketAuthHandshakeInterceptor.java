package com.sovereingschool.back_streaming.Configurations;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import com.sovereingschool.back_common.Utils.JwtUtil;

@Component
public class WebsocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtils;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        try {
            MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(request.getURI()).build()
                    .getQueryParams();
            String token = params.getFirst("token");
            System.out.println("Token: " + token);
            if (token == null || token.isEmpty()) {
                System.err.println("Error en el handshake de WebSocket: no hay token en la ruta");
                attributes.put("Error", "Error: no hay token en la ruta");
            }
            Authentication auth = this.jwtUtils.createAuthenticationFromToken(token);
            attributes.put("Auth", auth);
            String username = jwtUtils.getUsername(token);
            attributes.put("username", username);
            Long idUsuario = jwtUtils.getIdUsuario(token);
            attributes.put("idUsuario", idUsuario);

            // Aunque no tenga token, permites el handshake (validarás luego en el Handler)
            return true;
        } catch (Exception e) {
            System.err.println("Error en el handshake de WebSocket: " + e.getMessage());
            attributes.put("Error", e.getMessage());
            return true;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @Nullable Exception exception) {
        // No necesitamos hacer nada aquí
    }
}
