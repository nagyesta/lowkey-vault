package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.CreateSecretRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.UpdateSecretRequest;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
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

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@RequestMapping({"", "/vault/*/"})
public class SecretController extends BaseController<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity,
        KeyVaultSecretModel, DeletedKeyVaultSecretModel, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel,
        SecretEntityToV72ModelConverter, SecretEntityToV72SecretItemModelConverter,
        SecretEntityToV72SecretVersionItemModelConverter, SecretVaultFake> {

    @Autowired
    public SecretController(
            @NonNull final SecretEntityToV72ModelConverter secretEntityToV72ModelConverter,
            @NonNull final SecretEntityToV72SecretItemModelConverter secretEntityToV72SecretItemModelConverter,
            @NonNull final SecretEntityToV72SecretVersionItemModelConverter secretEntityToV72SecretVersionItemModelConverter,
            @NonNull final VaultService vaultService) {
        super(secretEntityToV72ModelConverter, secretEntityToV72SecretItemModelConverter,
                secretEntityToV72SecretVersionItemModelConverter, vaultService, VaultFake::secretVaultFake);
    }

    @PutMapping(value = "/secrets/{secretName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> create(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
                                                      @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                      @Valid @RequestBody final CreateSecretRequest request) {
        log.info("Received request to {} create secret: {} using API version: {}",
                baseUri.toString(), secretName, V_7_2);

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final VersionedSecretEntityId secretEntityId = createSecretWithAttributes(secretVaultFake, secretName, request);
        return ResponseEntity.ok(getModelById(secretVaultFake, secretEntityId));
    }

    @DeleteMapping(value = "/secrets/{secretName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> delete(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
                                                      @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} delete secret: {} using API version: {}",
                baseUri.toString(), secretName, V_7_2);

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final SecretEntityId entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.delete(entityId);
        final VersionedSecretEntityId latestVersion = secretVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(secretVaultFake, latestVersion));
    }

    @GetMapping(value = "/secrets/{secretName}/versions",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        log.info("Received request to {} list secret versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), secretName, maxResults, skipToken, V_7_2);

        return ResponseEntity
                .ok(getPageOfItemVersions(baseUri, secretName, maxResults, skipToken, "/secrets/" + secretName + "/versions"));
    }

    @GetMapping(value = "/secrets",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> listSecrets(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        log.info("Received request to {} list secrets, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, V_7_2);

        return ResponseEntity.ok(getPageOfItems(baseUri, maxResults, skipToken, "/secrets"));
    }

    @GetMapping(value = "/deletedsecrets",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> listDeletedSecrets(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        log.info("Received request to {} list deleted secrets, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, V_7_2);

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, maxResults, skipToken, "/deletedsecrets"));
    }

    @GetMapping(value = "/secrets/{secretName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get secret: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), secretName, V_7_2);

        return ResponseEntity.ok(getLatestEntityModel(baseUri, secretName));
    }

    @GetMapping(value = "/secrets/{secretName}/{secretVersion}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get secret: {} with version: {} using API version: {}",
                baseUri.toString(), secretName, secretVersion, V_7_2);

        final ReadOnlyKeyVaultSecretEntity keyVaultSecretEntity = getEntityByNameAndVersion(baseUri, secretName, secretVersion);
        return ResponseEntity.ok(convertDetails(keyVaultSecretEntity));
    }

    @PatchMapping(value = "/secrets/{secretName}/{secretVersion}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> updateVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @NonNull @Valid @RequestBody final UpdateSecretRequest request) {
        log.info("Received request to {} update secret: {} with version: {} using API version: {}",
                baseUri.toString(), secretName, secretVersion, V_7_2);

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final VersionedSecretEntityId entityId = versionedEntityId(baseUri, secretName, secretVersion);
        updateAttributes(secretVaultFake, entityId, request.getProperties());
        updateTags(secretVaultFake, entityId, request.getTags());
        return ResponseEntity.ok(getModelById(secretVaultFake, entityId));
    }

    @GetMapping(value = "/deletedsecrets/{secretName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> getDeletedSecret(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get deleted secret: {} using API version: {}",
                baseUri.toString(), secretName, V_7_2);

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final SecretEntityId entityId = new SecretEntityId(baseUri, secretName);
        final VersionedSecretEntityId latestVersion = secretVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(secretVaultFake, latestVersion));
    }

    @PostMapping(value = "/deletedsecrets/{secretName}/recover",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> recoverDeletedSecret(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} recover deleted secret: {} using API version: {}",
                baseUri.toString(), secretName, V_7_2);

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final SecretEntityId entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.recover(entityId);
        final VersionedSecretEntityId latestVersion = secretVaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(secretVaultFake, latestVersion));
    }

    @Override
    protected VersionedSecretEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedSecretEntityId(baseUri, name, version);
    }

    @Override
    protected SecretEntityId entityId(final URI baseUri, final String name) {
        return new SecretEntityId(baseUri, name);
    }

    private VersionedSecretEntityId createSecretWithAttributes(
            final SecretVaultFake secretVaultFake, final String secretName, final CreateSecretRequest request) {
        final SecretPropertiesModel properties = Objects.requireNonNullElse(request.getProperties(), new SecretPropertiesModel());
        final VersionedSecretEntityId secretEntityId = secretVaultFake
                .createSecretVersion(secretName, request.getValue(), request.getContentType());
        secretVaultFake.addTags(secretEntityId, request.getTags());
        secretVaultFake.setExpiry(secretEntityId, properties.getNotBefore(), properties.getExpiresOn());
        secretVaultFake.setEnabled(secretEntityId, properties.isEnabled());
        return secretEntityId;
    }
}
