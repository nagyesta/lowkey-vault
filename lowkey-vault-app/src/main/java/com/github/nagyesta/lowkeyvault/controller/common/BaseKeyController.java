package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.*;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreateDetailedInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyImportInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import io.jsonwebtoken.lang.Assert;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Objects;

@Slf4j
public abstract class BaseKeyController
        extends GenericEntityController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyVaultFake> {

    protected BaseKeyController(
            final VaultService vaultService,
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72KeyItemModelConverter itemConverter) {
        super(vaultService, modelConverter, itemConverter, VaultFake::keyVaultFake);
    }

    public ResponseEntity<KeyOperationsResult> encrypt(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            final String apiVersion,
            @Valid final KeyOperationsParameters request) {
        log.info("Received request to {} encrypt using key: {} with version: {} using API version: {}",
                baseUri, keyName, keyVersion, apiVersion);

        final var keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final var decodedBytes = request.getValue();
        final var encrypted = keyVaultKeyEntity.encryptBytes(decodedBytes, request.getAlgorithm(), request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), encrypted, request, baseUri));
    }

    public ResponseEntity<KeyOperationsResult> decrypt(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            final String apiVersion,
            @Valid final KeyOperationsParameters request) {
        log.info("Received request to {} decrypt using key: {} with version: {} using API version: {}",
                baseUri, keyName, keyVersion, apiVersion);

        final var keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final var decodedBytes = request.getValue();
        final var decrypted = keyVaultKeyEntity.decryptToBytes(decodedBytes, request.getAlgorithm(), request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), decrypted, request, baseUri));
    }

    public ResponseEntity<KeySignResult> sign(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            final String apiVersion,
            @Valid final KeySignParameters request) {
        log.info("Received request to {} sign using key: {} with version: {} using API version: {}",
                baseUri, keyName, keyVersion, apiVersion);

        final var keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final var decodedBytes = request.getValueAsBase64DecodedBytes();
        Assert.isTrue(decodedBytes != null, "Value must not be null.");
        final var signature = keyVaultKeyEntity.signBytes(decodedBytes, request.getAlgorithm());
        return ResponseEntity.ok(KeySignResult.forBytes(keyVaultKeyEntity.getId(), signature, baseUri));
    }

    public ResponseEntity<KeyVerifyResult> verify(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            final String apiVersion,
            @Valid final KeyVerifyParameters request) {
        log.info("Received request to {} verify using key: {} with version: {} using API version: {}",
                baseUri, keyName, keyVersion, apiVersion);

        final var keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final var digestBytes = request.getDigestAsBase64DecodedBytes();
        Assert.isTrue(digestBytes != null, "Digest must not be null.");
        final var decodedBytes = request.getValueAsBase64DecodedBytes();
        Assert.isTrue(decodedBytes != null, "Value must not be null.");
        final var result = keyVaultKeyEntity.verifySignedBytes(digestBytes, request.getAlgorithm(), decodedBytes);
        return ResponseEntity.ok(new KeyVerifyResult(result));
    }

    protected VersionedKeyEntityId createKeyWithAttributes(
            final KeyVaultFake keyVaultFake,
            final String keyName,
            final CreateKeyRequest request) {
        final var properties = Objects.requireNonNullElse(request.getProperties(), new KeyPropertiesModel());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        return keyVaultFake.createKeyVersion(keyName, KeyCreateDetailedInput.builder()
                .key(request.toKeyCreationInput())
                .keyOperations(request.getKeyOperations())
                .tags(request.getTags())
                .expiresOn(properties.getExpiry())
                .notBefore(properties.getNotBefore())
                .enabled(properties.isEnabled())
                .hsm(request.getKeyType().isHsm())
                .managed(false)
                .build());
    }

    protected VersionedKeyEntityId importKeyWithAttributes(
            final KeyVaultFake keyVaultFake,
            final String keyName,
            final ImportKeyRequest request) {
        final var properties = Objects
                .requireNonNullElse(request.getProperties(), new BasePropertiesUpdateModel());
        return keyVaultFake.importKeyVersion(keyName, KeyImportInput.builder()
                .key(request.getKey())
                .hsm(request.getHsm())
                .tags(request.getTags())
                .enabled(properties.getEnabled())
                .expiresOn(properties.getExpiresOn())
                .notBefore(properties.getNotBefore())
                .createdOn(null)
                .updatedOn(null)
                .managed(false)
                .build());
    }

    @Override
    protected VersionedKeyEntityId versionedEntityId(
            final URI baseUri,
            final String name,
            final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(
            final URI baseUri,
            final String name) {
        return new KeyEntityId(baseUri, name);
    }
}
