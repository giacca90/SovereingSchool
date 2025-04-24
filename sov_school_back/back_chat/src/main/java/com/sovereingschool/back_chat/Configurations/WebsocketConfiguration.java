package com.sovereingschool.back_chat.Configurations;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer, DisposableBean {

    private static class CustomWebSocketHandler extends AbstractWebSocketHandler {

        @Override
        public void afterConnectionEstablished(@NonNull WebSocketSession session) {
            startPingPong(session);
        }

        @Override
        public void handlePongMessage(@NonNull WebSocketSession session, @NonNull PongMessage message) {
            missedPongs.set(0);
        }

        private void startPingPong(WebSocketSession session) {
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    if (session.isOpen()) {
                        if (missedPongs.get() >= 3) {
                            session.close(CloseStatus.SESSION_NOT_RELIABLE);
                            System.out.println("Sesión cerrada después de 3 pings sin respuesta");
                            return;
                        }
                        session.sendMessage(new PingMessage());
                        missedPongs.incrementAndGet();
                    }
                } catch (IOException e) {
                    System.err.println("Error enviando ping: " + e);
                    try {
                        session.close(CloseStatus.SERVER_ERROR);
                    } catch (IOException ex) {
                        System.err.println("Error al cerrar la conexión websocket: " + ex);
                    }
                }
            }, 0, 10, TimeUnit.SECONDS);
        }
    }

    @Autowired
    private static AtomicInteger missedPongs;

    @Autowired
    private static ScheduledExecutorService scheduler;

    @Autowired
    private WebSocketAuthInterceptor authInterceptor; // ChannelInterceptor, no HandshakeInterceptor

    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/init_chat", "/queue/errors");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-socket")
                .setAllowedOriginPatterns("https://localhost:4200", "wss://localhost:4200")
                .withSockJS(); // No interceptor aquí, ya que es ChannelInterceptor, no HandshakeInterceptor
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(authInterceptor); // Aquí es donde va tu authInterceptor real
    }

    @Bean
    public CustomWebSocketHandler webSocketHandler() {
        return new CustomWebSocketHandler();
    }

    @Override
    public void destroy() {
        pingScheduler.shutdown();
        try {
            if (!pingScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                pingScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            pingScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
