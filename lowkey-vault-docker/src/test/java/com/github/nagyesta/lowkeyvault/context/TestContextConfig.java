package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import io.cucumber.spring.ScenarioScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestContextConfig {

    /**
     * Default base URI.
     */
    public static final String HTTPS_LOCALHOST_8443 = "https://localhost:8443";

    @Bean
    public ApacheHttpClientProvider provider() {
        return new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443);
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
