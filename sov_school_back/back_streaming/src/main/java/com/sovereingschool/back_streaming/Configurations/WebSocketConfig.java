package com.sovereingschool.back_streaming.Configurations;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;
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

    @Autowired
    private AuthHandshakeInterceptor authHandshakeInterceptor;

    // Executor para tareas de ping-pong
    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        // Registrar el handler para la webcam
        WebRTCSignalingHandler handler = new WebRTCSignalingHandler(webSocketTaskExecutor(), streamingService);
        registry.addHandler(handler, "/live-webcam")
                .setAllowedOrigins("*")// Cambiar "*" por dominios específicos en producción
                .addInterceptors(authHandshakeInterceptor);
        // Registrar el handler para OBS
        registry.addHandler(new OBSWebSocketHandler(webSocketTaskExecutor(), streamingService), "/live-obs")
                .setAllowedOrigins("*") // Cambia "*" a los dominios permitidos en producción
                .addInterceptors(authHandshakeInterceptor);
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();

        // Incrementar tamaño del buffer para manejar mensajes grandes
        container.setMaxTextMessageBufferSize(1024 * 1024); // 512 KB para mensajes de texto
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // 512 KB para mensajes binarios

        // Configurar tiempo de espera y heartbeats
        container.setAsyncSendTimeout(30_000L); // 30 segundos para enviar mensajes asíncronos
        container.setMaxSessionIdleTimeout(3_600_000L); // 1 hora para sesiones inactivas

        return container;
    }

    @Bean(name = "webSocketTaskExecutor")
    public Executor webSocketTaskExecutor() {
        // Crear un pool de hilos para manejar mensajes en paralelo
        return Executors.newFixedThreadPool(50);
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
/*
 * @Configuration
 * 
 * @EnableWebSocket
 * public class WebSocketConfig implements WebSocketConfigurer {
 * 
 * private final WebSocketHandler myWebSocketHandler;
 * private final AuthHandshakeInterceptor authHandshakeInterceptor;
 * 
 * public WebSocketConfig(WebSocketHandler myWebSocketHandler,
 * AuthHandshakeInterceptor authHandshakeInterceptor) {
 * this.myWebSocketHandler = myWebSocketHandler;
 * this.authHandshakeInterceptor = authHandshakeInterceptor;
 * }
 * 
 * @Override
 * public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry
 * registry) {
 * registry.addHandler(myWebSocketHandler, "/ws/stream")
 * .addInterceptors(authHandshakeInterceptor)
 * .setAllowedOrigins("*"); // o restringe los origins si quieres
 * }
 * }
 */
/*
 * @Configuration
 * 
 * @EnableWebSocketMessageBroker
 * public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
 * 
 * @Autowired
 * private JwtHandshakeInterceptor jwtHandshakeInterceptor;
 * 
 * @Autowired
 * private JwtChannelInterceptor jwtChannelInterceptor;
 * 
 * @Autowired
 * private TaskScheduler messageBrokerTaskScheduler;
 * 
 * // 1) Beans para el contenedor de WebSocket
 * 
 * @Bean
 * public ServletServerContainerFactoryBean createWebSocketContainer() {
 * ServletServerContainerFactoryBean container = new
 * ServletServerContainerFactoryBean();
 * container.setMaxTextMessageBufferSize(1024 * 1024);
 * container.setMaxBinaryMessageBufferSize(1024 * 1024);
 * container.setAsyncSendTimeout(30_000L); // 30 segundos
 * container.setMaxSessionIdleTimeout(3_600_000L); // 1 hora
 * return container;
 * }
 * 
 * // 2) Bean de TaskExecutor para WebSocket
 * 
 * @Bean(name = "webSocketTaskExecutor")
 * public ThreadPoolTaskExecutor webSocketTaskExecutor() {
 * ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
 * executor.setCorePoolSize(50);
 * executor.setMaxPoolSize(100);
 * executor.setQueueCapacity(500);
 * executor.setThreadNamePrefix("ws-thread-");
 * executor.initialize();
 * return executor;
 * }
 * 
 * // 3) Bean para el scheduler de pings
 * 
 * @Bean
 * public ScheduledExecutorService pingScheduler() {
 * return Executors.newScheduledThreadPool(1);
 * }
 * 
 * // 4) Registrar los endpoints de STOMP con SockJS y el HandshakeInterceptor
 * 
 * @Override
 * public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
 * registry
 * .addEndpoint("/live-webcam")
 * .addInterceptors(jwtHandshakeInterceptor)
 * .setAllowedOrigins("https://localhost:4200");
 * 
 * registry
 * .addEndpoint("/live-obs")
 * .addInterceptors(jwtHandshakeInterceptor)
 * .setAllowedOrigins("https://localhost:4200");
 * }
 * 
 * // 5) Configurar el broker STOMP con heartbeats automáticos
 * 
 * @Override
 * public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
 * registry
 * .enableSimpleBroker("/topic", "/queue")
 * .setHeartbeatValue(new long[] { 10_000, 10_000 })
 * .setTaskScheduler(messageBrokerTaskScheduler);
 * registry.setApplicationDestinationPrefixes("/app");
 * registry.setUserDestinationPrefix("/user");
 * }
 * 
 * // 6) Configurar interceptor para validar JWT en cada mensaje entrante
 * 
 * @Override
 * public void configureClientInboundChannel(@NonNull ChannelRegistration
 * registration) {
 * registration.taskExecutor(webSocketTaskExecutor());
 * registration.interceptors(jwtChannelInterceptor);
 * }
 * 
 * // 7) Opcional: configurar el outbound también con el executor
 * 
 * @Override
 * public void configureClientOutboundChannel(@NonNull ChannelRegistration
 * registration) {
 * registration
 * .taskExecutor(webSocketTaskExecutor());
 * }
 * }
 */