package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Slf4j
public abstract class CommonKeyPolicyController
        extends BaseKeyController {

    protected CommonKeyPolicyController(
            @NonNull final KeyConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    public ResponseEntity<KeyRotationPolicyModel> getRotationPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} get rotation policy: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());
        return getRotationPolicyResponseEntity(getVaultByUri(baseUri), entityId(baseUri, keyName), baseUri);
    }

    public ResponseEntity<KeyRotationPolicyModel> updateRotationPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            @NonNull @Valid @Validated(Update.class) final KeyRotationPolicyModel request) {
        log.info("Received request to {} update rotation policy: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());
        final var keyEntityId = entityId(baseUri, keyName);
        request.setKeyEntityId(keyEntityId);
        final var rotationPolicy = registry().rotationPolicyEntityConverter(apiVersion()).convert(request);
        final var keyVaultFake = getVaultByUri(baseUri);
        keyVaultFake.setRotationPolicy(rotationPolicy);
        return getRotationPolicyResponseEntity(keyVaultFake, keyEntityId, baseUri);
    }

    private ResponseEntity<KeyRotationPolicyModel> getRotationPolicyResponseEntity(
            final KeyVaultFake keyVaultFake,
            final KeyEntityId keyEntityId,
            final URI baseUri) {
        final var policy = keyVaultFake.rotationPolicy(keyEntityId);
        final var converter = registry()
                .rotationPolicyModelConverter(apiVersion());
        return ResponseEntity.ok(converter.convert(policy, baseUri));
    }
}
