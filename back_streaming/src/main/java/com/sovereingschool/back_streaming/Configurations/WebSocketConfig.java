package com.sovereingschool.back_streaming.Configurations;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import com.sovereingschool.back_streaming.Controllers.OBSWebSocketHandler;
import com.sovereingschool.back_streaming.Controllers.WebRTCSignalingHandler;
import com.sovereingschool.back_streaming.Services.StreamingService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private StreamingService streamingService;

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        // Registrar el handler para la webcam
        WebRTCSignalingHandler handler = new WebRTCSignalingHandler(webSocketTaskExecutor(), streamingService);
        registry.addHandler(handler, "/live-webcam")
                .setAllowedOrigins("*"); // Cambiar "*" por dominios específicos en producción

        // Registrar el handler para OBS
        registry.addHandler(new OBSWebSocketHandler(streamingService), "/live-obs")
                .setAllowedOrigins("*"); // Cambia "*" a los dominios permitidos en producción

    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        // Incrementar tamaño del buffer para manejar mensajes grandes
        container.setMaxTextMessageBufferSize(512 * 1024); // 512 KB para mensajes de texto
        container.setMaxBinaryMessageBufferSize(512 * 1024); // 512 KB para mensajes binarios

        // Configurar tiempo de espera en conexiones WebSocket
        container.setAsyncSendTimeout(30_000L); // 30 segundos
        container.setMaxSessionIdleTimeout(60_000L); // 60 segundos

        return container;
    }

    @Bean(name = "webSocketTaskExecutor")
    public Executor webSocketTaskExecutor() {
        // Crear un pool de hilos para manejar mensajes en paralelo
        return Executors.newFixedThreadPool(10);
    }
}
