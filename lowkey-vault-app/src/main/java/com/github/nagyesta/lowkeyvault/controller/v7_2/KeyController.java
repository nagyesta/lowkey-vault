package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.service.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
public class KeyController {

    private static final String KEY_NAME_PATTERN = "^[0-9a-zA-Z-]+$";
    private static final String VERSION_NAME_PATTERN = "^[0-9a-f]{32}$";
    private static final String API_VERSION_7_2 = "api-version=" + V_7_2;
    private static final String DEFAULT_MAX = "10000";
    private static final String SKIP_ZERO = "0";
    private static final String MAX_RESULTS_PARAM = "maxresults";
    private static final String SKIP_TOKEN_PARAM = "$skiptoken";

    private final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter;
    private final KeyEntityToV72ItemModelConverter keyEntityToV72ItemModelConverter;
    private final VaultService vaultService;

    @Autowired
    public KeyController(@NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
                         @NonNull final KeyEntityToV72ItemModelConverter keyEntityToV72ItemModelConverter,
                         @NonNull final VaultService vaultService) {
        this.keyEntityToV72ModelConverter = keyEntityToV72ModelConverter;
        this.keyEntityToV72ItemModelConverter = keyEntityToV72ItemModelConverter;
        this.vaultService = vaultService;
    }

    @PostMapping(value = "/keys/{keyName}/create",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> create(@PathVariable @Valid @Pattern(regexp = KEY_NAME_PATTERN) final String keyName,
                                                   @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                   @Valid @RequestBody final CreateKeyRequest request) {
        log.info("Received request to {} create key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final KeyVaultStub keyVaultStub = vaultService.findByUri(baseUri).keyVaultStub();

        final VersionedKeyEntityId keyEntityId = createKeyWithAttributes(keyVaultStub, keyName, request);
        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = keyVaultStub.getEntity(keyEntityId);

        return ResponseEntity.ok(keyEntityToV72ModelConverter.convert(keyVaultKeyEntity));
    }

    @GetMapping(value = "/keys/{keyName}/versions",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyItemListModel> versions(
            @PathVariable @Valid @Pattern(regexp = KEY_NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        log.info("Received request to {} list key versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), keyName, maxResults, skipToken, V_7_2);

        final VaultStub vaultStub = vaultService.findByUri(baseUri);
        final KeyVaultStub keyVaultStub = vaultStub.keyVaultStub();

        final KeyEntityId keyEntityId = new KeyEntityId(vaultStub.baseUri(), keyName, null);
        final Deque<String> versions = keyVaultStub.getVersions(keyEntityId);
        final List<KeyVaultKeyItemModel> items = versions.stream()
                .skip(skipToken)
                .limit(maxResults)
                .map(v -> new VersionedKeyEntityId(vaultStub.baseUri(), keyName, v))
                .map(keyVaultStub::getEntity)
                .map(keyEntityToV72ItemModelConverter::convert)
                .collect(Collectors.toList());

        URI nextUri = null;
        if (items.size() == maxResults) {
            nextUri = keyEntityId.asUri(versionsSuffix(maxResults, skipToken + items.size()));
        }
        return ResponseEntity.ok(new KeyVaultKeyItemListModel(items, nextUri));
    }

    @GetMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> get(
            @PathVariable @Valid @Pattern(regexp = KEY_NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get key: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), keyName, V_7_2);

        final VaultStub vaultStub = vaultService.findByUri(baseUri);
        final KeyVaultStub keyVaultStub = vaultStub.keyVaultStub();

        final KeyEntityId keyQuery = new KeyEntityId(vaultStub.baseUri(), keyName, null);
        final VersionedKeyEntityId keyEntityId = keyVaultStub.getLatestVersionOfEntity(keyQuery);
        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = keyVaultStub.getEntity(keyEntityId);

        return ResponseEntity.ok(keyEntityToV72ModelConverter.convert(keyVaultKeyEntity));
    }

    @GetMapping(value = "/keys/{keyName}/{keyVersion}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = KEY_NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getKeyByNameAndVersion(keyName, keyVersion, baseUri);

        return ResponseEntity.ok(keyEntityToV72ModelConverter.convert(keyVaultKeyEntity));
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/encrypt", "/keys/{keyName}/{keyVersion}/wrap"},
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> encrypt(
            @PathVariable @Valid @Pattern(regexp = KEY_NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        log.info("Received request to {} encrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getKeyByNameAndVersion(keyName, keyVersion, baseUri);
        final byte[] encrypted = keyVaultKeyEntity.encryptBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector(), request.getAdditionalAuthData(), request.getAuthenticationTag());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), encrypted, request));
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/decrypt", "/keys/{keyName}/{keyVersion}/unwrap"},
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> decrypt(
            @PathVariable @Valid @Pattern(regexp = KEY_NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        log.info("Received request to {} decrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getKeyByNameAndVersion(keyName, keyVersion, baseUri);
        final byte[] decrypted = keyVaultKeyEntity.decryptToBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector(), request.getAdditionalAuthData(), request.getAuthenticationTag());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), decrypted, request));
    }

    private ReadOnlyKeyVaultKeyEntity getKeyByNameAndVersion(final String keyName, final String keyVersion, final URI baseUri) {
        final VaultStub vaultStub = vaultService.findByUri(baseUri);
        final KeyVaultStub keyVaultStub = vaultStub.keyVaultStub();

        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultStub.baseUri(), keyName, keyVersion);
        return keyVaultStub.getEntity(keyEntityId);
    }

    private String versionsSuffix(final int maxResults, final int skip) {
        return "versions?" + API_VERSION_7_2 + "&" + SKIP_TOKEN_PARAM + "=" + skip + "&" + MAX_RESULTS_PARAM + "=" + maxResults;
    }

    private VersionedKeyEntityId createKeyWithAttributes(
            final KeyVaultStub keyVaultStub, final String keyName, final CreateKeyRequest request) {
        final KeyPropertiesModel properties = Objects.requireNonNullElse(request.getProperties(), new KeyPropertiesModel());
        final VersionedKeyEntityId keyEntityId = keyVaultStub.createKeyVersion(keyName, request.toKeyCreationInput());
        keyVaultStub.addTags(keyEntityId, request.getTags());
        Optional.ofNullable(properties.getRecoveryLevel())
                .ifPresent(level -> keyVaultStub.setRecovery(keyEntityId, level, properties.getRecoverableDays()));
        keyVaultStub.setExpiry(keyEntityId, properties.getNotBefore(), properties.getExpiresOn());
        keyVaultStub.setEnabled(keyEntityId, properties.isEnabled());
        keyVaultStub.setKeyOperations(keyEntityId, request.getKeyOperations());
        return keyEntityId;
    }

}
