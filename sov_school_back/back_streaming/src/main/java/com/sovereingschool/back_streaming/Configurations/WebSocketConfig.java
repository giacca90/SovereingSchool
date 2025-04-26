package com.sovereingschool.back_streaming.Configurations;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtHandshakeInterceptor jwtHandshakeInterceptor;

    @Autowired
    private JwtChannelInterceptor jwtChannelInterceptor;

    @Autowired
    private TaskScheduler messageBrokerTaskScheduler;

    // 1) Beans para el contenedor de WebSocket
    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024 * 1024);
        container.setMaxBinaryMessageBufferSize(1024 * 1024);
        container.setAsyncSendTimeout(30_000L); // 30 segundos
        container.setMaxSessionIdleTimeout(3_600_000L); // 1 hora
        return container;
    }

    // 2) Bean de TaskExecutor para WebSocket
    @Bean(name = "webSocketTaskExecutor")
    public ThreadPoolTaskExecutor webSocketTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(50);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ws-thread-");
        executor.initialize();
        return executor;
    }

    // 3) Bean para el scheduler de pings
    @Bean
    public ScheduledExecutorService pingScheduler() {
        return Executors.newScheduledThreadPool(1);
    }

    // 4) Registrar los endpoints de STOMP con SockJS y el HandshakeInterceptor
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        registry
                .addEndpoint("/live-webcam")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("https://localhost:4200");

        registry
                .addEndpoint("/live-obs")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOrigins("https://localhost:4200");
    }

    // 5) Configurar el broker STOMP con heartbeats automáticos
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        registry
                .enableSimpleBroker("/topic", "/queue")
                .setHeartbeatValue(new long[] { 10_000, 10_000 })
                .setTaskScheduler(messageBrokerTaskScheduler);
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    // 6) Configurar interceptor para validar JWT en cada mensaje entrante
    @Override
    public void configureClientInboundChannel(@NonNull ChannelRegistration registration) {
        registration.taskExecutor(webSocketTaskExecutor());
        registration.interceptors(jwtChannelInterceptor);
    }

    // 7) Opcional: configurar el outbound también con el executor
    @Override
    public void configureClientOutboundChannel(@NonNull ChannelRegistration registration) {
        registration
                .taskExecutor(webSocketTaskExecutor());
    }
}
