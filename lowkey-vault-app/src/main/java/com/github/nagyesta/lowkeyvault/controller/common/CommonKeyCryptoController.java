package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyOperationsResult;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeySignResult;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVerifyResult;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeySignParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyVerifyParameters;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;

@Slf4j
public abstract class CommonKeyCryptoController extends BaseKeyController {

    protected CommonKeyCryptoController(@NonNull final KeyConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    public ResponseEntity<KeyOperationsResult> encrypt(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            @Valid final KeyOperationsParameters request) {
        log.info("Received request to {} encrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, apiVersion());

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] encrypted = keyVaultKeyEntity.encryptBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), encrypted, request, baseUri));
    }

    public ResponseEntity<KeyOperationsResult> decrypt(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            @Valid final KeyOperationsParameters request) {
        log.info("Received request to {} decrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, apiVersion());

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] decrypted = keyVaultKeyEntity.decryptToBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), decrypted, request, baseUri));
    }

    public ResponseEntity<KeySignResult> sign(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            @Valid final KeySignParameters request) {
        log.info("Received request to {} sign using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, apiVersion());

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] signature = keyVaultKeyEntity.signBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm());
        return ResponseEntity.ok(KeySignResult.forBytes(keyVaultKeyEntity.getId(), signature, baseUri));
    }

    public ResponseEntity<KeyVerifyResult> verify(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            @Valid final KeyVerifyParameters request) {
        log.info("Received request to {} verify using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, apiVersion());

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final boolean result = keyVaultKeyEntity.verifySignedBytes(request.getDigestAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getValueAsBase64DecodedBytes());
        return ResponseEntity.ok(new KeyVerifyResult(result));
    }

}
