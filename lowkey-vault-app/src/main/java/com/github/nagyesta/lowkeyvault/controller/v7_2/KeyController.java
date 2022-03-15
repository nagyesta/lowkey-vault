package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
public class KeyController extends BaseController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyEntityToV72ModelConverter, KeyEntityToV72KeyItemModelConverter, KeyEntityToV72KeyVersionItemModelConverter,
        KeyVaultFake> {

    @Autowired
    public KeyController(@NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
                         @NonNull final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
                         @NonNull final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
                         @NonNull final VaultService vaultService) {
        super(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService, VaultFake::keyVaultFake);
    }

    @PostMapping(value = "/keys/{keyName}/create",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> create(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                   @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                   @Valid @RequestBody final CreateKeyRequest request) {
        log.info("Received request to {} create key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId keyEntityId = createKeyWithAttributes(keyVaultFake, keyName, request);
        return ResponseEntity.ok(getModelById(keyVaultFake, keyEntityId));
    }

    @PutMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> importKey(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                      @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                      @Valid @RequestBody final ImportKeyRequest request) {
        log.info("Received request to {} import key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId keyEntityId = importKeyWithAttributes(keyVaultFake, keyName, request);
        return ResponseEntity.ok(getModelById(keyVaultFake, keyEntityId));
    }

    @DeleteMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> delete(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                   @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} delete key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        keyVaultFake.delete(entityId);
        final VersionedKeyEntityId latestVersion = keyVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(keyVaultFake, latestVersion));
    }

    @GetMapping(value = "/keys/{keyName}/versions",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        log.info("Received request to {} list key versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), keyName, maxResults, skipToken, V_7_2);

        return ResponseEntity.ok(getPageOfItemVersions(baseUri, keyName, maxResults, skipToken, "/keys/" + keyName + "/versions"));
    }

    @GetMapping(value = "/keys",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> listKeys(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        log.info("Received request to {} list keys, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, V_7_2);

        return ResponseEntity.ok(getPageOfItems(baseUri, maxResults, skipToken, "/keys"));
    }

    @GetMapping(value = "/deletedkeys",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> listDeletedKeys(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        log.info("Received request to {} list deleted keys, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, V_7_2);

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, maxResults, skipToken, "/deletedkeys"));
    }

    @GetMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get key: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        return ResponseEntity.ok(getLatestEntityModel(baseUri, keyName));
    }

    @GetMapping(value = "/keys/{keyName}/{keyVersion}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        return ResponseEntity.ok(convertDetails(keyVaultKeyEntity));
    }

    @PatchMapping(value = "/keys/{keyName}/{keyVersion}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> updateVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @NonNull @Valid @RequestBody final UpdateKeyRequest request) {
        log.info("Received request to {} update key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final VersionedKeyEntityId entityId = versionedEntityId(baseUri, keyName, keyVersion);
        Optional.ofNullable(request.getKeyOperations())
                .ifPresent(operations -> keyVaultFake.setKeyOperations(entityId, operations));
        updateAttributes(keyVaultFake, entityId, request.getProperties());
        updateTags(keyVaultFake, entityId, request.getTags());
        return ResponseEntity.ok(getModelById(keyVaultFake, entityId));
    }

    @GetMapping(value = "/deletedkeys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> getDeletedKey(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                          @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get deleted key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        final VersionedKeyEntityId latestVersion = keyVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(keyVaultFake, latestVersion));
    }

    @PostMapping(value = "/deletedkeys/{keyName}/recover",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> recoverDeletedKey(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                              @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} recover deleted key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        keyVaultFake.recover(entityId);
        final VersionedKeyEntityId latestVersion = keyVaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(keyVaultFake, latestVersion));
    }

    @DeleteMapping(value = "/deletedkeys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeDeleted(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                             @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} purge deleted key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

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
        return keyEntityId;
    }
}
