package com.sovereingschool.back_base.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Configuration
public class WebClientConfig {

	@Bean
	public WebClient webClient(WebClient.Builder builder) throws Exception {
		// Configura un SslContext que confía en todos los certificados (autofirmados
		// incluidos)
		SslContext sslContext = SslContextBuilder.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE) // Ignora validaciones de confianza
				.build();

		// Configura un HttpClient con este SslContext
		HttpClient httpClient = HttpClient.create()
				.secure(t -> t.sslContext(sslContext)) // Aplica la configuración SSL
				.wiretap("reactor.netty.http.client.HttpClient",
						io.netty.handler.logging.LogLevel.DEBUG, // Nivel de logs (opcional)
						AdvancedByteBufFormat.TEXTUAL); // Ver contenido textual (opcional)

		// Conecta el HttpClient al WebClient
		ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

		// Construye el WebClient
		return builder.clientConnector(connector)
				.codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // Ejemplo para
				// manejar
				// respuestas
				// grandes
				.build();
	}
}
