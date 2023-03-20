package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.CreateSecretRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.UpdateSecretRequest;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;

@Slf4j
public abstract class CommonSecretController extends BaseSecretController {

    protected CommonSecretController(@NonNull final SecretConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    public ResponseEntity<KeyVaultSecretModel> create(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            @Valid final CreateSecretRequest request) {
        log.info("Received request to {} create secret: {} using API version: {}",
                baseUri.toString(), secretName, apiVersion());

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final VersionedSecretEntityId secretEntityId = createSecretWithAttributes(secretVaultFake, secretName, request);
        return ResponseEntity.ok(getModelById(secretVaultFake, secretEntityId, baseUri, true));
    }

    public ResponseEntity<DeletedKeyVaultSecretModel> delete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri) {
        log.info("Received request to {} delete secret: {} using API version: {}",
                baseUri.toString(), secretName, apiVersion());

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final SecretEntityId entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.delete(entityId);
        final VersionedSecretEntityId latestVersion = secretVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(secretVaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> versions(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list secret versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), secretName, maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItemVersions(baseUri, secretName, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + ("/secrets/" + secretName + "/versions")))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> listSecrets(
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list secrets, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/secrets"))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultSecretItemModel>> listDeletedSecrets(
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list deleted secrets, (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/deletedsecrets"))
                .build()));
    }

    public ResponseEntity<KeyVaultSecretModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri) {
        log.info("Received request to {} get secret: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), secretName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, secretName));
    }

    public ResponseEntity<KeyVaultSecretModel> getWithVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            final URI baseUri) {
        log.info("Received request to {} get secret: {} with version: {} using API version: {}",
                baseUri.toString(), secretName, secretVersion, apiVersion());
        return ResponseEntity.ok(getSpecificEntityModel(baseUri, secretName, secretVersion));
    }

    public ResponseEntity<KeyVaultSecretModel> updateVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            final URI baseUri,
            @Valid final UpdateSecretRequest request) {
        log.info("Received request to {} update secret: {} with version: {} using API version: {}",
                baseUri.toString(), secretName, secretVersion, apiVersion());

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final VersionedSecretEntityId entityId = versionedEntityId(baseUri, secretName, secretVersion);
        updateAttributes(secretVaultFake, entityId, request.getProperties());
        updateTags(secretVaultFake, entityId, request.getTags());
        return ResponseEntity.ok(getModelById(secretVaultFake, entityId, baseUri, true));
    }

    public ResponseEntity<KeyVaultSecretModel> getDeletedSecret(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri) {
        log.info("Received request to {} get deleted secret: {} using API version: {}",
                baseUri.toString(), secretName, apiVersion());

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final SecretEntityId entityId = new SecretEntityId(baseUri, secretName);
        final VersionedSecretEntityId latestVersion = secretVaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(secretVaultFake, latestVersion, baseUri, false));
    }

    public ResponseEntity<Void> purgeDeleted(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri) {
        log.info("Received request to {} purge deleted secret: {} using API version: {}",
                baseUri.toString(), secretName, apiVersion());

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final SecretEntityId entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.purge(entityId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<KeyVaultSecretModel> recoverDeletedSecret(
            @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            final URI baseUri) {
        log.info("Received request to {} recover deleted secret: {} using API version: {}",
                baseUri.toString(), secretName, apiVersion());

        final SecretVaultFake secretVaultFake = getVaultByUri(baseUri);
        final SecretEntityId entityId = new SecretEntityId(baseUri, secretName);
        secretVaultFake.recover(entityId);
        final VersionedSecretEntityId latestVersion = secretVaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(secretVaultFake, latestVersion, baseUri, true));
    }
}
