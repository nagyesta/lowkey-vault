package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Optional;

@Slf4j
public abstract class BaseCertificateController
        extends GenericEntityController<CertificateEntityId, VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity,
        KeyVaultCertificateModel, DeletedKeyVaultCertificateModel, KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel,
        CertificateVaultFake> {

    /**
     * Default parameter value for including the pending certificates.
     */
    protected static final String TRUE = "true";
    /**
     * Parameter name for including the pending certificates.
     */
    protected static final String INCLUDE_PENDING_PARAM = "includePending";
    private final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter;
    private final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter;
    private final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter;

    protected BaseCertificateController(
            final VaultService vaultService,
            final CertificateEntityToV73ModelConverter modelConverter,
            final CertificateEntityToV73CertificateItemModelConverter itemConverter,
            final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter,
            final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter,
            final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter) {
        super(vaultService, modelConverter, itemConverter, VaultFake::certificateVaultFake);
        this.pendingOperationConverter = pendingOperationConverter;
        this.issuancePolicyConverter = issuancePolicyConverter;
        this.lifetimeActionConverter = lifetimeActionConverter;
    }

    @Override
    protected KeyVaultCertificateModel getModelById(
            final CertificateVaultFake entityVaultFake,
            final VersionedCertificateEntityId entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final var model = super.getModelById(entityVaultFake, entityId, baseUri, includeDisabled);
        populateLifetimeActions(entityVaultFake, entityId, model.getPolicy());
        return model;
    }

    @Override
    protected DeletedKeyVaultCertificateModel getDeletedModelById(
            final CertificateVaultFake entityVaultFake,
            final VersionedCertificateEntityId entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final var model = super.getDeletedModelById(entityVaultFake, entityId, baseUri, includeDisabled);
        populateLifetimeActions(entityVaultFake, entityId, model.getPolicy());
        return model;
    }

    protected void populateLifetimeActions(
            final CertificateVaultFake vaultFake,
            final VersionedCertificateEntityId entityId,
            @Nullable final CertificatePolicyModel model) {
        if (model == null) {
            return;
        }
        Optional.ofNullable(vaultFake.lifetimeActionPolicy(entityId))
                .map(lifetimeActionConverter::convert)
                .ifPresent(model::setLifetimeActions);
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingCreate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get pending create certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);
        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final var readOnlyEntity = vaultFake.getEntities()
                .getReadOnlyEntity(entityId);
        return ResponseEntity.ok(pendingOperationConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingDelete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get pending delete certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);
        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = vaultFake.getDeletedEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final var readOnlyEntity = vaultFake
                .getDeletedEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok(pendingOperationConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} get certificate policy: {} with version: -LATEST- using API version: {}",
                baseUri, certificateName, apiVersion);
        final var vaultFake = getVaultByUri(baseUri);
        final var latest = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final var entity = vaultFake.getEntities().getReadOnlyEntity(latest);
        final var model = issuancePolicyConverter.convert(entity, baseUri);
        populateLifetimeActions(vaultFake, latest, model);
        return ResponseEntity.ok(model);
    }

    public ResponseEntity<CertificatePolicyModel> updatePolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion,
            @Valid final CertificatePolicyModel request) {
        log.info("Received request to {} update certificate issuance policy: {} with version: -LATEST- using API version: {}",
                baseUri, certificateName, apiVersion);
        final var vaultFake = getVaultByUri(baseUri);
        final var latest = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        CertificateRequestMapperUtil.updateIssuancePolicy(vaultFake, latest, request);
        final var entity = vaultFake.getEntities().getReadOnlyEntity(latest);
        final var model = issuancePolicyConverter.convert(entity, baseUri);
        populateLifetimeActions(vaultFake, latest, model);
        return ResponseEntity.ok(model);
    }

    @Override
    protected VersionedCertificateEntityId versionedEntityId(
            final URI baseUri,
            final String name,
            final String version) {
        return new VersionedCertificateEntityId(baseUri, name, version);
    }

    @Override
    protected CertificateEntityId entityId(
            final URI baseUri,
            final String name) {
        return new CertificateEntityId(baseUri, name);
    }
}
