package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultPendingCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Objects;

@Slf4j
public abstract class CommonCertificatePolicyController extends BaseCertificateController {

    protected CommonCertificatePolicyController(
            @NonNull final CertificateConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingCreate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get pending create certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake.getEntities()
                .getReadOnlyEntity(entityId);
        return ResponseEntity.ok(registry().pendingOperationConverters(apiVersion()).convert(readOnlyEntity, baseUri));
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
        return ResponseEntity.ok(registry().pendingOperationConverters(apiVersion()).convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate policy: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId latest = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final ReadOnlyKeyVaultCertificateEntity entity = vaultFake.getEntities().getReadOnlyEntity(latest);
        final CertificatePolicyModel model = registry().issuancePolicyConverters(apiVersion()).convert(entity, baseUri);
        populateLifetimeActions(vaultFake, latest, Objects.requireNonNull(model));
        return ResponseEntity.ok(model);
    }

    public ResponseEntity<CertificatePolicyModel> updatePolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            @Valid final CertificatePolicyModel request) {
        log.info("Received request to {} update certificate issuance policy: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId latest = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        CertificateRequestMapperUtil.updateIssuancePolicy(vaultFake, latest, request);
        final ReadOnlyKeyVaultCertificateEntity entity = vaultFake.getEntities().getReadOnlyEntity(latest);
        final CertificatePolicyModel model = registry().issuancePolicyConverters(apiVersion()).convert(entity, baseUri);
        populateLifetimeActions(vaultFake, latest, Objects.requireNonNull(model));
        return ResponseEntity.ok(model);
    }
}
