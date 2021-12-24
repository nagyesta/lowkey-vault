package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class AppConfiguration {

    @Value("${LOWKEY_VAULT_NAMES:primary,secondary}")
    private String autoRegisterVaults;
    @Value("${server.port}")
    private int port;

    @Bean
    public VaultService vaultService() {
        final VaultServiceImpl service = new VaultServiceImpl();
        log.info("Starting up vault with port: {} , auto-registering vaults: '{}'", port, autoRegisterVaults);
        Stream.of(
                        "https://localhost:" + port,
                        "https://default.lowkey-vault:" + port,
                        "https://default.lowkey-vault.local:" + port
                )
                .map(URI::create).forEach(service::create);
        Optional.ofNullable(autoRegisterVaults)
                .filter(StringUtils::hasText)
                .map(StringUtils::commaDelimitedListToStringArray)
                .map(array -> Arrays.stream(array)
                        .filter(StringUtils::hasText)
                        .map(vaultName -> StringUtils.trimTrailingCharacter(vaultName, '/'))
                        .map(vaultName -> "https://" + vaultName + ".localhost:" + port)
                        .map(URI::create))
                .orElse(Stream.of()).forEach(service::create);

        return service;
    }
}
