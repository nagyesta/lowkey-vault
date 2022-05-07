package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeySignParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyVerifyParameters;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;

@Slf4j
public abstract class CommonKeyCryptoController extends GenericEntityController<KeyEntityId, VersionedKeyEntityId,
        ReadOnlyKeyVaultKeyEntity, KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyEntityToV72ModelConverter, KeyEntityToV72KeyItemModelConverter, KeyEntityToV72KeyVersionItemModelConverter,
        KeyVaultFake> {

    protected CommonKeyCryptoController(
            @NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
            @NonNull final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
            @NonNull final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
            @NonNull final VaultService vaultService) {
        super(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService, VaultFake::keyVaultFake);
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
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), encrypted, request));
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
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), decrypted, request));
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
        return ResponseEntity.ok(KeySignResult.forBytes(keyVaultKeyEntity.getId(), signature));
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

    @Override
    protected VersionedKeyEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(final URI baseUri, final String name) {
        return new KeyEntityId(baseUri, name);
    }

}
