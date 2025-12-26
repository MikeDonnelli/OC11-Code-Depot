package com.example.hospital.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

@Configuration
public class SslConfig {

    /**
     * Configure WebClient to accept self-signed certificates in dev mode
     * ⚠️ WARNING: This is ONLY for development/testing
     * In production, use proper CA-signed certificates
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        try {
            // Create SSL context that trusts all certificates (INSECURE - dev only)
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();

            HttpClient httpClient = HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));

            return WebClient.builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient));

        } catch (SSLException e) {
            throw new RuntimeException("Failed to configure SSL context", e);
        }
    }
}
