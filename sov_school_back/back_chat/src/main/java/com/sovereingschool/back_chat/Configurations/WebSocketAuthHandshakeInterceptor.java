package com.sovereingschool.back_chat.Configurations;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.sovereingschool.back_common.Utils.JwtUtil;

@Component
public class WebSocketAuthHandshakeInterceptor implements HandshakeInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @NonNull Map<String, Object> attributes) {

        try {
            // Obtener el parámetro 'token' de la URL
            String token = request.getURI().getQuery();
            String accessToken = null;

            if (token != null && token.startsWith("token=")) {
                accessToken = token.substring(6); // Eliminar "token="
            }

            if (accessToken != null) {
                // Validar el token JWT
                Authentication authentication = jwtUtil.createAuthenticationFromToken(accessToken);

                // Guardar la autenticación en sessionAttributes
                attributes.put("user", authentication);
                return true;
            }

            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            System.err.println("Falta el token Bearer en la URL");
            writeResponseBody(response, "Falta el token Bearer en la URL");
            return false;

        } catch (AuthenticationException e) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            System.err.println("Token inválido en el HandShake: " + e.getMessage());
            writeResponseBody(response, "Token inválido en el HandShake: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response,
            @NonNull WebSocketHandler wsHandler,
            @Nullable Exception exception) {
        // Nada que hacer aquí
    }

    private void writeResponseBody(ServerHttpResponse response, String message) {
        try {
            response.getHeaders().setContentType(MediaType.TEXT_PLAIN);
            OutputStream os = ((ServletServerHttpResponse) response).getServletResponse().getOutputStream();
            os.write(message.getBytes(StandardCharsets.UTF_8));
            os.flush();
        } catch (IOException ioException) {
            System.err.println("Error al escribir el mensaje de respuesta WebSocket: " + ioException.getMessage());
        }
    }
}
