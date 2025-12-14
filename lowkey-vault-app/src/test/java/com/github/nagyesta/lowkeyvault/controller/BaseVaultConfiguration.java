package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.mapper.MapperConfig;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

@Profile("vault")
@Configuration
@Import(MapperConfig.class)
public class BaseVaultConfiguration {

    @Bean
    public VaultService vaultService() {
        return new VaultServiceImpl(uri -> uri);
    }
}
