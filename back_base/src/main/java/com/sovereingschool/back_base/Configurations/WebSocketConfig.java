package com.sovereingschool.back_base.Configurations;

import java.io.IOException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.sovereingschool.back_base.Services.CursoService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final CursoService cursoService;

    public WebSocketConfig(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        try {
            WebRTCSignalingHandler handler = new WebRTCSignalingHandler(cursoService);
            registry.addHandler(handler, "/live-webcam")
                    .setAllowedOrigins("*"); // Cambiar "*" por dominios específicos en producción
        } catch (IOException e) {
            System.err.println("Error al crear el WebRTCSignalingHandler: " + e.getMessage());
        }
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(102400); // 100KB para mensajes de texto
        container.setMaxBinaryMessageBufferSize(102400); // 100KB para mensajes binarios
        return container;
    }
}
