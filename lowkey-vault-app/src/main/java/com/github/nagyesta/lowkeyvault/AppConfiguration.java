package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.context.util.VaultUriUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Configuration
@Slf4j
public class AppConfiguration {

    private static final int PAYLOAD_LENGTH_BYTES = 512 * 1024;
    private static final String SKIP_AUTO_REGISTRATION = "-";
    @Value("${LOWKEY_VAULT_NAMES:primary,secondary}")
    private String autoRegisterVaults;
    @Value("${server.port}")
    private int port;

    @Bean
    public VaultService vaultService() throws IOException {
        final VaultServiceImpl service = new VaultServiceImpl();
        if (!SKIP_AUTO_REGISTRATION.equals(autoRegisterVaults)) {
            autoRegisterVaults(service);
        }
        return service;
    }

    private void autoRegisterVaults(final VaultServiceImpl service) {
        log.info("Starting up vault with port: {} , auto-registering vaults: '{}'", port, autoRegisterVaults);
        Stream.of(
                        VaultUriUtil.vaultUri("localhost", port),
                        VaultUriUtil.vaultUri("default.lowkey-vault", port),
                        VaultUriUtil.vaultUri("default.lowkey-vault.local", port)
                )
                .forEach(service::create);
        Optional.ofNullable(autoRegisterVaults)
                .filter(StringUtils::hasText)
                .map(StringUtils::commaDelimitedListToStringArray)
                .map(array -> Arrays.stream(array)
                        .filter(StringUtils::hasText)
                        .map(vaultName -> StringUtils.trimTrailingCharacter(vaultName, '/'))
                        .map(vaultName -> VaultUriUtil.vaultUri(vaultName + ".localhost", port)))
                .orElse(Stream.of()).forEach(service::create);
        log.info("Vaults registered!");
    }

    @Bean
    @ConditionalOnExpression("${LOWKEY_DEBUG_REQUEST_LOG:false}")
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        final CommonsRequestLoggingFilter requestLoggingFilter = new CommonsRequestLoggingFilter();
        requestLoggingFilter.setIncludeClientInfo(true);
        requestLoggingFilter.setIncludeQueryString(true);
        requestLoggingFilter.setIncludePayload(true);
        requestLoggingFilter.setMaxPayloadLength(PAYLOAD_LENGTH_BYTES);
        return requestLoggingFilter;
    }
}
