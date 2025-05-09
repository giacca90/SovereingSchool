package com.sovereingschool.back_chat.Configurations;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer, DisposableBean {

    @Autowired
    private WebSocketAuthInterceptor authInterceptor;

    @Autowired
    private WebSocketAuthHandshakeInterceptor handshakeInterceptor;

    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry config) {
        config.enableSimpleBroker("/init_chat");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry.addEndpoint("/chat-socket")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor); // Aquí se agrega el HandshakeInterceptor
    }

    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.interceptors(securityContextChannelInterceptor(), authInterceptor);
    }

    @Bean
    public SecurityContextChannelInterceptor securityContextChannelInterceptor() {
        return new SecurityContextChannelInterceptor();
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
