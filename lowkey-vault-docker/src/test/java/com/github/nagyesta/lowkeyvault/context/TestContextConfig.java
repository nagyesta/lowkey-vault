package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import io.cucumber.spring.ScenarioScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestContextConfig {

    /**
     * The host port mapped to the SSL port of the container.
     */
    public static final String CONTAINER_PORT = "8444";
    /**
     * The hostname we can use to access the container from the host.
     */
    public static final String CONTAINER_HOST = "localhost";
    /**
     * The authority we can use to access the container from the host.
     */
    public static final String CONTAINER_AUTHORITY = CONTAINER_HOST + ":" + CONTAINER_PORT;
    /**
     * Default base URI.
     */
    public static final String DEFAULT_CONTAINER_URL = "https://" + CONTAINER_HOST + ":" + CONTAINER_PORT;

    @Bean
    public ApacheHttpClientProvider provider() {
        return new ApacheHttpClientProvider(DEFAULT_CONTAINER_URL);
    }

    @Bean
    @ScenarioScope
    public KeyTestContext keyContext() {
        return new KeyTestContext(provider());
    }

    @Bean
    @ScenarioScope
    public SecretTestContext secretContext() {
        return new SecretTestContext(provider());
    }
}
