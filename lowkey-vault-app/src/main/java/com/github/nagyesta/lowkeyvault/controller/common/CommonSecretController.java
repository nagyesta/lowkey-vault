package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.CreateSecretRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.UpdateSecretRequest;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.net.URI;

@Slf4j
public abstract class CommonSecretController extends BaseSecretController {

    protected CommonSecretController(
            final VaultService vaultService,
            final SecretEntityToV72ModelConverter modelConverter,
            final SecretEntityToV72SecretItemModelConverter itemConverter) {
        super(vaultService, modelConverter, itemConverter);
    }

    public ResponseEntity<KeyVaultSecretModel> create(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion,
            @Valid final CreateSecretRequest request) {
        log.info("Received request to {} create secret: {} using API version: {}",
                baseUri, secretName, apiVersion);
        final var secretVaultFake = getVaultByUri(baseUri);
        final var secretEntityId = createSecretWithAttributes(secretVaultFake, secretName, request);
        return ResponseEntity.ok(getModelById(secretVaultFake, secretEntityId, baseUri, true));
    }

    public ResponseEntity<DeletedKeyVaultSecretModel> delete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} delete secret: {} using API version: {}",
                baseUri, secretName, apiVersion);
        final var secretVaultFake = getVaultByUri(baseUri);
        final var entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.delete(entityId);
        final var latestVersion = secretVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(secretVaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> versions(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list secret versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri, secretName, maxResults, skipToken, apiVersion);
        return ResponseEntity.ok(getPageOfItemVersions(baseUri, secretName, PaginationContext
                .builder()
                .apiVersion(apiVersion)
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + ("/secrets/" + secretName + "/versions")))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> listSecrets(
            final URI baseUri,
            final String apiVersion,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list secrets, (max results: {}, skip: {}) using API version: {}",
                baseUri, maxResults, skipToken, apiVersion);
        return ResponseEntity.ok(getPageOfItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion)
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/secrets"))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultSecretItemModel>> listDeletedSecrets(
            final URI baseUri,
            final String apiVersion,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list deleted secrets, (max results: {}, skip: {}) using API version: {}",
                baseUri, maxResults, skipToken, apiVersion);
        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion)
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/deletedsecrets"))
                .build()));
    }

    public ResponseEntity<KeyVaultSecretModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get secret: {} with version: -LATEST- using API version: {}",
                baseUri, secretName, apiVersion);
        return ResponseEntity.ok(getLatestEntityModel(baseUri, secretName));
    }

    public ResponseEntity<KeyVaultSecretModel> getWithVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get secret: {} with version: {} using API version: {}",
                baseUri, secretName, secretVersion, apiVersion);
        return ResponseEntity.ok(getSpecificEntityModel(baseUri, secretName, secretVersion));
    }

    public ResponseEntity<KeyVaultSecretModel> updateVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            final URI baseUri,
            final String apiVersion,
            @Valid final UpdateSecretRequest request) {
        log.info("Received request to {} update secret: {} with version: {} using API version: {}",
                baseUri, secretName, secretVersion, apiVersion);
        final var secretVaultFake = getVaultByUri(baseUri);
        final var entityId = versionedEntityId(baseUri, secretName, secretVersion);
        updateAttributes(secretVaultFake, entityId, request.getProperties());
        updateTags(secretVaultFake, entityId, request.getTags());
        return ResponseEntity.ok(getModelById(secretVaultFake, entityId, baseUri, true));
    }

    public ResponseEntity<KeyVaultSecretModel> getDeletedSecret(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get deleted secret: {} using API version: {}",
                baseUri, secretName, apiVersion);
        final var secretVaultFake = getVaultByUri(baseUri);
        final var entityId = new SecretEntityId(baseUri, secretName);
        final var latestVersion = secretVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(secretVaultFake, latestVersion, baseUri, false));
    }

    public ResponseEntity<Void> purgeDeleted(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} purge deleted secret: {} using API version: {}",
                baseUri, secretName, apiVersion);
        final var secretVaultFake = getVaultByUri(baseUri);
        final var entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.purge(entityId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<KeyVaultSecretModel> recoverDeletedSecret(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} recover deleted secret: {} using API version: {}",
                baseUri, secretName, apiVersion);
        final var secretVaultFake = getVaultByUri(baseUri);
        final var entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.recover(entityId);
        final var latestVersion = secretVaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(secretVaultFake, latestVersion, baseUri, true));
    }

    @Override
    protected VersionedSecretEntityId versionedEntityId(
            final URI baseUri,
            final String name,
            final String version) {
        return new VersionedSecretEntityId(baseUri, name, version);
    }

    @Override
    protected SecretEntityId entityId(
            final URI baseUri,
            final String name) {
        return new SecretEntityId(baseUri, name);
    }
}
