package com.sovereingschool.back_chat.Configurations;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

    // Executor para tareas de ping-pong
    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/init_chat");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-socket").setAllowedOrigins("*");
    }

    @Bean
    public ScheduledExecutorService pingScheduler() {
        return pingScheduler;
    }

    /**
     * Método para iniciar el ping-pong.
     * Envía un `PING` cada 10 segundos y verifica que se reciba un `PONG`.
     * Si no se recibe el `PONG`, la conexión se cerrará.
     */
    public void startPingPong(WebSocketSession session) {
        pingScheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen()) {
                    // Enviar mensaje de Ping
                    session.sendMessage(new PingMessage());
                } else {
                    pingScheduler.shutdown();
                }
            } catch (Exception e) {
                System.err.println("Error enviando PING: " + e.getMessage());
                try {
                    session.close();
                } catch (Exception closeEx) {
                    System.err.println("Error cerrando sesión: " + closeEx.getMessage());
                }
            }
        }, 0, 10, TimeUnit.SECONDS); // Intervalo de 10 segundos
    }
}
