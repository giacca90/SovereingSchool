package com.sovereingschool.back_base.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Registra el handler para WebRTC signaling
        registry.addHandler(new WebRTCSignalingHandler(), "/live-webcam")
                .setAllowedOrigins("*"); // Cambiar '*' por dominios específicos en producción
    }
}