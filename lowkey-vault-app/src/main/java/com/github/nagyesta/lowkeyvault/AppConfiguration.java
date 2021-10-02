package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.stream.Stream;

@Configuration
public class AppConfiguration {

    @Bean
    public VaultService vaultService() {
        final VaultServiceImpl service = new VaultServiceImpl();
        Stream.of("https://localhost:8443", "https://default.lowkey-vault:8443", "https://default.lowkey-vault.local:8443")
                .map(URI::create).forEach(service::create);
        return service;
    }
}
