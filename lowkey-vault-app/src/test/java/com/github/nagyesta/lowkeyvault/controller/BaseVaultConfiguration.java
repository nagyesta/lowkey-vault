package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.function.Function;

@Profile("vault")
@Configuration
public class BaseVaultConfiguration {

    @Bean
    public VaultService vaultService() {
        return new VaultServiceImpl(Function.identity());
    }
}
