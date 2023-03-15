package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73IssuancePolicyModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73PendingCertificateOperationModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.LifetimeActionsPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultPendingCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.function.Function;

@Slf4j
public abstract class CommonCertificatePolicyController
        extends BaseEntityReadController<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, CertificateVaultFake> {

    private final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter;
    private final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter;
    private final LifetimeActionsPolicyToV73ModelConverter lifetimeActionsConverter;

    protected CommonCertificatePolicyController(
            @lombok.NonNull final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter,
            @lombok.NonNull final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter,
            @lombok.NonNull final LifetimeActionsPolicyToV73ModelConverter lifetimeActionsConverter,
            @NonNull final VaultService vaultService,
            @NonNull final Function<VaultFake, CertificateVaultFake> toEntityVault) {
        super(vaultService, toEntityVault);
        this.pendingOperationConverter = pendingOperationConverter;
        this.issuancePolicyConverter = issuancePolicyConverter;
        this.lifetimeActionsConverter = lifetimeActionsConverter;
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingCreate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get pending create certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = vaultFake
                .getEntities().getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake
                .getEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok(pendingOperationConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingDelete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get pending delete certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = vaultFake.getDeletedEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake
                .getDeletedEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok(pendingOperationConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate policy: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId latest = vaultFake.getEntities().getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity entity = vaultFake.getEntities().getReadOnlyEntity(latest);
        final CertificatePolicyModel model = issuancePolicyConverter.convert(entity, baseUri);
        lifetimeActionsConverter.populateLifetimeActions(vaultFake, latest, model::setLifetimeActions);
        return ResponseEntity.ok(model);
    }

    @Override
    protected VersionedCertificateEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedCertificateEntityId(baseUri, name, version);
    }

    @Override
    protected CertificateEntityId entityId(final URI baseUri, final String name) {
        return new CertificateEntityId(baseUri, name);
    }
}
