package com.sovereingschool.back_base.Configurations;

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
        // Crear WebRTCSignalingHandler como un bean de Spring y pasar CursoService a su
        // constructor
        WebRTCSignalingHandler handler = new WebRTCSignalingHandler(cursoService);
        registry.addHandler(handler, "/live-webcam")
                .setAllowedOrigins("*"); // Cambiar "*" por dominios específicos en producción
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(65536); // Set text message buffer size to 64KB
        container.setMaxBinaryMessageBufferSize(65536); // Set binary message buffer size to 64KB
        return container;
    }
}
