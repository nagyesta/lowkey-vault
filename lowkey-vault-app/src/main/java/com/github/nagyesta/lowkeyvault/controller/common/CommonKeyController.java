package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.ImportKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.UpdateKeyRequest;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.Optional;

@Slf4j
public abstract class CommonKeyController extends BaseKeyController {

    protected CommonKeyController(@NonNull final KeyConverterRegistry registry,
                                  @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    public ResponseEntity<KeyVaultKeyModel> create(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            @Valid final CreateKeyRequest request) {
        log.info("Received request to {} create key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId keyEntityId = createKeyWithAttributes(keyVaultFake, keyName, request);
        return ResponseEntity.ok(getModelById(keyVaultFake, keyEntityId, baseUri, true));
    }

    public ResponseEntity<KeyVaultKeyModel> importKey(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            @Valid final ImportKeyRequest request) {
        log.info("Received request to {} import key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId keyEntityId = importKeyWithAttributes(keyVaultFake, keyName, request);
        return ResponseEntity.ok(getModelById(keyVaultFake, keyEntityId, baseUri, true));
    }

    public ResponseEntity<KeyVaultKeyModel> delete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} delete key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        keyVaultFake.delete(entityId);
        final VersionedKeyEntityId latestVersion = keyVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(keyVaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> versions(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list key versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), keyName, maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItemVersions(baseUri, keyName, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/keys/" + keyName + "/versions"))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> listKeys(
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list keys, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/keys"))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultKeyItemModel>> listDeletedKeys(
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list deleted keys, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/deletedkeys"))
                .build()));
    }

    public ResponseEntity<KeyVaultKeyModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} get key: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, keyName));
    }

    public ResponseEntity<KeyVaultKeyModel> getWithVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri) {
        log.info("Received request to {} get key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, apiVersion());

        return ResponseEntity.ok(getSpecificEntityModel(baseUri, keyName, keyVersion));
    }

    public ResponseEntity<KeyVaultKeyModel> updateVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            final URI baseUri,
            @Valid final UpdateKeyRequest request) {
        log.info("Received request to {} update key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId entityId = versionedEntityId(baseUri, keyName, keyVersion);
        Optional.ofNullable(request.getKeyOperations())
                .ifPresent(operations -> keyVaultFake.setKeyOperations(entityId, operations));
        updateAttributes(keyVaultFake, entityId, request.getProperties());
        updateTags(keyVaultFake, entityId, request.getTags());
        return ResponseEntity.ok(getModelById(keyVaultFake, entityId, baseUri, true));
    }

    public ResponseEntity<DeletedKeyVaultKeyModel> getDeletedKey(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} get deleted key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        final VersionedKeyEntityId latestVersion = keyVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(keyVaultFake, latestVersion, baseUri, false));
    }

    public ResponseEntity<KeyVaultKeyModel> recoverDeletedKey(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} recover deleted key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        keyVaultFake.recover(entityId);
        final VersionedKeyEntityId latestVersion = keyVaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(keyVaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<Void> purgeDeleted(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} purge deleted key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        keyVaultFake.purge(entityId);
        return ResponseEntity.noContent().build();
    }
}
