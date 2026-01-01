package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.createCertificateWithAttributes;
import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.importCertificateWithAttributes;

@Slf4j
public abstract class CommonCertificateController extends BaseCertificateController {

    private final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter;

    protected CommonCertificateController(
            final VaultService vaultService,
            final CertificateEntityToV73ModelConverter modelConverter,
            final CertificateEntityToV73CertificateItemModelConverter itemConverter,
            final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter,
            final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter,
            final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter) {
        super(vaultService, modelConverter, itemConverter, pendingOperationConverter, issuancePolicyConverter, lifetimeActionConverter);
        this.pendingOperationConverter = pendingOperationConverter;
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> create(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion,
            @Valid final CreateCertificateRequest request) {
        log.info("Received request to {} create certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);

        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = createCertificateWithAttributes(vaultFake, certificateName, request);
        final var readOnlyEntity = vaultFake.getEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.accepted().body(pendingOperationConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultCertificateModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get certificate: {} with version: -LATEST- using API version: {}",
                baseUri, certificateName, apiVersion);

        return ResponseEntity.ok(getLatestEntityModel(baseUri, certificateName));
    }

    public ResponseEntity<KeyVaultCertificateModel> getWithVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get certificate: {} with version: {} using API version: {}",
                baseUri, certificateName, certificateVersion, apiVersion);

        return ResponseEntity.ok(getSpecificEntityModel(baseUri, certificateName, certificateVersion));
    }

    public ResponseEntity<KeyVaultCertificateModel> importCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion,
            @Valid @RequestBody final CertificateImportRequest request) {
        log.info("Received request to {} import certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);

        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = importCertificateWithAttributes(vaultFake, certificateName, request);
        return ResponseEntity.ok().body(getSpecificEntityModel(baseUri, certificateName, entityId.version()));
    }

    public ResponseEntity<DeletedKeyVaultCertificateModel> delete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} delete certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);

        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = entityId(baseUri, certificateName);
        vaultFake.delete(entityId);
        final var latestVersion = vaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(vaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<DeletedKeyVaultCertificateModel> getDeletedCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get deleted certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);

        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = entityId(baseUri, certificateName);
        final var latestVersion = vaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(vaultFake, latestVersion, baseUri, false));
    }

    public ResponseEntity<KeyVaultCertificateModel> recoverDeletedCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} recover deleted certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);

        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = entityId(baseUri, certificateName);
        vaultFake.recover(entityId);
        final var latestVersion = vaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(vaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<Void> purgeDeleted(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} purge deleted certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);

        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = entityId(baseUri, certificateName);
        vaultFake.purge(entityId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> versions(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list certificate versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri, certificateName, maxResults, skipToken, apiVersion);

        return ResponseEntity.ok(getPageOfItemVersions(baseUri, certificateName, PaginationContext
                .builder()
                .apiVersion(apiVersion)
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/certificates/" + certificateName + "/versions"))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> listCertificates(
            final URI baseUri,
            final String apiVersion,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri, maxResults, skipToken, includePending, apiVersion);

        return ResponseEntity.ok(getPageOfItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion)
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/certificates"))
                .additionalParameters(Map.of(INCLUDE_PENDING_PARAM, String.valueOf(includePending)))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel>> listDeletedCertificates(
            final URI baseUri,
            final String apiVersion,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list deleted certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri, maxResults, skipToken, includePending, apiVersion);

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion)
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/deletedcertificates"))
                .additionalParameters(Map.of(INCLUDE_PENDING_PARAM, String.valueOf(includePending)))
                .build()));
    }

    public ResponseEntity<KeyVaultCertificateModel> updateCertificateProperties(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            final URI baseUri,
            final String apiVersion,
            @Valid @RequestBody final UpdateCertificateRequest request) {
        log.info("Received request to {} update certificate: {} with version: {} using API version: {}",
                baseUri, certificateName, certificateVersion, apiVersion);

        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = versionedEntityId(baseUri, certificateName, certificateVersion);
        Optional.ofNullable(request.getAttributes())
                .map(CertificatePropertiesModel::isEnabled)
                .ifPresent(enabled -> vaultFake.setEnabled(entityId, enabled));
        vaultFake.clearTags(entityId);
        vaultFake.addTags(entityId, Objects.requireNonNullElse(request.getTags(), Collections.emptyMap()));
        final var model = getModelById(vaultFake, entityId, baseUri, true);
        model.setPolicy(null);
        return ResponseEntity.ok(model);
    }
}
