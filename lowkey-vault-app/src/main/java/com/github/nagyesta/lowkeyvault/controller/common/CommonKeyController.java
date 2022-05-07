package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.ImportKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.UpdateKeyRequest;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public abstract class CommonKeyController extends GenericEntityController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyEntityToV72ModelConverter, KeyEntityToV72KeyItemModelConverter, KeyEntityToV72KeyVersionItemModelConverter,
        KeyVaultFake> {

    protected CommonKeyController(@NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
                                  @NonNull final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
                                  @NonNull final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
                                  @NonNull final VaultService vaultService) {
        super(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService, VaultFake::keyVaultFake);
    }

    public ResponseEntity<KeyVaultKeyModel> create(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            @Valid final CreateKeyRequest request) {
        log.info("Received request to {} create key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId keyEntityId = createKeyWithAttributes(keyVaultFake, keyName, request);
        return ResponseEntity.ok(getModelById(keyVaultFake, keyEntityId));
    }

    public ResponseEntity<KeyVaultKeyModel> importKey(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            @Valid final ImportKeyRequest request) {
        log.info("Received request to {} import key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId keyEntityId = importKeyWithAttributes(keyVaultFake, keyName, request);
        return ResponseEntity.ok(getModelById(keyVaultFake, keyEntityId));
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
        return ResponseEntity.ok(getDeletedModelById(keyVaultFake, latestVersion));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> versions(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list key versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), keyName, maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItemVersions(baseUri, keyName, maxResults, skipToken, "/keys/" + keyName + "/versions"));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> listKeys(
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list keys, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItems(baseUri, maxResults, skipToken, "/keys"));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> listDeletedKeys(
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list deleted keys, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, maxResults, skipToken, "/deletedkeys"));
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

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        return ResponseEntity.ok(convertDetails(keyVaultKeyEntity));
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
        return ResponseEntity.ok(getModelById(keyVaultFake, entityId));
    }

    public ResponseEntity<KeyVaultKeyModel> getDeletedKey(
            @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            final URI baseUri) {
        log.info("Received request to {} get deleted key: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        final VersionedKeyEntityId latestVersion = keyVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(keyVaultFake, latestVersion));
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
        return ResponseEntity.ok(getModelById(keyVaultFake, latestVersion));
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

    @Override
    protected VersionedKeyEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(final URI baseUri, final String name) {
        return new KeyEntityId(baseUri, name);
    }

    private VersionedKeyEntityId createKeyWithAttributes(
            final KeyVaultFake keyVaultFake, final String keyName, final CreateKeyRequest request) {
        final KeyPropertiesModel properties = Objects.requireNonNullElse(request.getProperties(), new KeyPropertiesModel());
        final VersionedKeyEntityId keyEntityId = keyVaultFake.createKeyVersion(keyName, request.toKeyCreationInput());
        keyVaultFake.setKeyOperations(keyEntityId, request.getKeyOperations());
        keyVaultFake.addTags(keyEntityId, request.getTags());
        keyVaultFake.setExpiry(keyEntityId, properties.getNotBefore(), properties.getExpiresOn());
        keyVaultFake.setEnabled(keyEntityId, properties.isEnabled());
        //no need to set managed property as this endpoint cannot create managed entities by definition
        return keyEntityId;
    }

    private VersionedKeyEntityId importKeyWithAttributes(
            final KeyVaultFake keyVaultFake, final String keyName, final ImportKeyRequest request) {
        final BasePropertiesUpdateModel properties = Objects.requireNonNullElse(request.getProperties(), new BasePropertiesUpdateModel());
        Assert.isTrue(request.getHsm() == null || request.getHsm() == request.getKey().getKeyType().isHsm(),
                "When HSM property is set in request, key type must match it.");
        final VersionedKeyEntityId keyEntityId = keyVaultFake.importKeyVersion(keyName, request.getKey());
        final JsonWebKeyImportRequest keyImport = request.getKey();
        keyVaultFake.setKeyOperations(keyEntityId, keyImport.getKeyOps());
        keyVaultFake.addTags(keyEntityId, request.getTags());
        keyVaultFake.setExpiry(keyEntityId, properties.getNotBefore(), properties.getExpiresOn());
        keyVaultFake.setEnabled(keyEntityId, Objects.requireNonNullElse(properties.getEnabled(), true));
        //no need to set managed property as this endpoint cannot create managed entities by definition
        return keyEntityId;
    }
}
