package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.context.util.VaultUriUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultServiceImpl;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import static com.github.nagyesta.lowkeyvault.context.util.VaultUriUtil.replacePortWith;

@Setter
@Configuration
@Slf4j
public class AppConfiguration {

    private static final int PAYLOAD_LENGTH_BYTES = 512 * 1024;
    private static final String SKIP_AUTO_REGISTRATION = "-";
    @Value("${LOWKEY_VAULT_NAMES:primary,secondary}")
    private String autoRegisterVaults;
    @Value("${server.port}")
    private int port;
    @Value("${LOWKEY_VAULT_ALIASES:}")
    private String aliases;
    @Value("${LOWKEY_VAULT_RELAXED_PORTS:false}")
    private boolean useRelaxedPorts;

    @Bean
    public VaultService vaultService() throws IOException {
        final VaultService service = new VaultServiceImpl(portMapper());
        if (!SKIP_AUTO_REGISTRATION.equals(autoRegisterVaults)) {
            autoRegisterVaults(service);
        }
        return service;
    }

    @Bean
    public Function<URI, URI> portMapper() {
        return Optional.of(useRelaxedPorts)
                .filter(BooleanUtils::isTrue)
                .map(use -> (Function<URI, URI>) uri -> replacePortWith(uri, port))
                .orElse(Function.identity());
    }

    private void autoRegisterVaults(final VaultService service) {
        log.info("Starting up vault with port: {} , auto-registering vaults: '{}'", port, autoRegisterVaults);
        doAutoRegisterVaults(service);
        doAddVaultAliases(service);
        log.info("Vaults registered!");
    }

    void doAutoRegisterVaults(final VaultService service) {
        service.create(VaultUriUtil.vaultUri("127.0.0.1", port));
        service.create(VaultUriUtil.vaultUri("localhost", port));
        service.create(VaultUriUtil.vaultUri("default.lowkey-vault", port));
        service.create(VaultUriUtil.vaultUri("default.lowkey-vault.local", port));
        Optional.ofNullable(autoRegisterVaults)
                .filter(StringUtils::hasText)
                .map(StringUtils::commaDelimitedListToStringArray)
                .stream()
                .flatMap(Arrays::stream)
                .filter(StringUtils::hasText)
                .map(vaultName -> StringUtils.trimTrailingCharacter(vaultName, '/'))
                .map(vaultName -> VaultUriUtil.vaultUri(vaultName + ".localhost", port))
                .forEach(service::create);
    }

    void doAddVaultAliases(final VaultService service) {
        Optional.ofNullable(aliases)
                .filter(StringUtils::hasText)
                .map(StringUtils::commaDelimitedListToStringArray)
                .stream()
                .flatMap(Arrays::stream)
                .filter(StringUtils::hasText)
                .forEach(pair -> {
                    final String[] aliasPair = StringUtils.delimitedListToStringArray(pair, "=");
                    Assert.isTrue(aliasPair.length == 2,
                            "Each alias pair must be in the 'vaultHost=aliasAuthority' format.");
                    final URI baseUri = VaultUriUtil.vaultUri(aliasPair[0], port);
                    final URI alias = VaultUriUtil.aliasUri(aliasPair[1], port);
                    service.updateAlias(baseUri, alias, null);
                });
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
