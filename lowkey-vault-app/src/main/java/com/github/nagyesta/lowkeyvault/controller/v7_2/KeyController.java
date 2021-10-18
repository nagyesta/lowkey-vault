package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.UpdateKeyRequest;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
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
@RequestMapping({"", "/vault/*/"})
public class KeyController extends BaseController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyEntityToV72ModelConverter, KeyEntityToV72KeyItemModelConverter, KeyEntityToV72KeyVersionItemModelConverter,
        KeyVaultStub> {

    @Autowired
    public KeyController(@NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
                         @NonNull final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
                         @NonNull final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
                         @NonNull final VaultService vaultService) {
        super(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService, VaultStub::keyVaultStub);
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

        final KeyVaultStub keyVaultStub = getVaultByUri(baseUri);
        final VersionedKeyEntityId keyEntityId = createKeyWithAttributes(keyVaultStub, keyName, request);
        return ResponseEntity.ok(getModelById(keyVaultStub, keyEntityId));
    }

    @DeleteMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> delete(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                   @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} delete key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultStub keyVaultStub = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        keyVaultStub.delete(entityId);
        final VersionedKeyEntityId latestVersion = keyVaultStub.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(keyVaultStub, latestVersion));
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

        final KeyVaultStub keyVaultStub = getVaultByUri(baseUri);
        final VersionedKeyEntityId entityId = versionedEntityId(baseUri, keyName, keyVersion);
        Optional.ofNullable(request.getKeyOperations())
                .ifPresent(operations -> keyVaultStub.setKeyOperations(entityId, operations));
        updateAttributes(keyVaultStub, entityId, request.getProperties());
        updateTags(keyVaultStub, entityId, request.getTags());
        return ResponseEntity.ok(getModelById(keyVaultStub, entityId));
    }

    @GetMapping(value = "/deletedkeys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> getDeletedKey(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                          @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get deleted key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultStub keyVaultStub = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        final VersionedKeyEntityId latestVersion = keyVaultStub.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(keyVaultStub, latestVersion));
    }

    @PostMapping(value = "/deletedkeys/{keyName}/recover",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> recoverDeletedKey(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                              @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} recover deleted key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultStub keyVaultStub = getVaultByUri(baseUri);
        final KeyEntityId entityId = new KeyEntityId(baseUri, keyName);
        keyVaultStub.recover(entityId);
        final VersionedKeyEntityId latestVersion = keyVaultStub.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(keyVaultStub, latestVersion));
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/encrypt", "/keys/{keyName}/{keyVersion}/wrap"},
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> encrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        log.info("Received request to {} encrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] encrypted = keyVaultKeyEntity.encryptBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), encrypted, request));
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/decrypt", "/keys/{keyName}/{keyVersion}/unwrap"},
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> decrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        log.info("Received request to {} decrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] decrypted = keyVaultKeyEntity.decryptToBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), decrypted, request));
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
            final KeyVaultStub keyVaultStub, final String keyName, final CreateKeyRequest request) {
        final KeyPropertiesModel properties = Objects.requireNonNullElse(request.getProperties(), new KeyPropertiesModel());
        final VersionedKeyEntityId keyEntityId = keyVaultStub.createKeyVersion(keyName, request.toKeyCreationInput());
        keyVaultStub.setKeyOperations(keyEntityId, request.getKeyOperations());
        keyVaultStub.addTags(keyEntityId, request.getTags());
        keyVaultStub.setExpiry(keyEntityId, properties.getNotBefore(), properties.getExpiresOn());
        keyVaultStub.setEnabled(keyEntityId, properties.isEnabled());
        return keyEntityId;
    }
}
