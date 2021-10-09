package com.github.nagyesta.lowkeyvault.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientProviderConfig {

    /**
     * Default base URI.
     */
    public static final String HTTPS_LOCALHOST_8443 = "https://localhost:8443";

    @Bean
    public ApacheHttpClientProvider provider() {
        return new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443);
    }
}
