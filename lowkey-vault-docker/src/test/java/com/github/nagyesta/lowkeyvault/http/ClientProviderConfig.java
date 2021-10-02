package com.github.nagyesta.lowkeyvault.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientProviderConfig {

    @Bean
    public ApacheHttpClientProvider provider() {
        return new ApacheHttpClientProvider("https://localhost:8443");
    }
}
