package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultPendingCertificateModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

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
        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final var readOnlyEntity = vaultFake.getEntities()
                .getReadOnlyEntity(entityId);
        return ResponseEntity.ok(registry().pendingOperationConverters(apiVersion()).convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> pendingDelete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get pending delete certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final var vaultFake = getVaultByUri(baseUri);
        final var entityId = vaultFake.getDeletedEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final var readOnlyEntity = vaultFake
                .getDeletedEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.ok(registry().pendingOperationConverters(apiVersion()).convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate policy: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final var vaultFake = getVaultByUri(baseUri);
        final var latest = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        final var entity = vaultFake.getEntities().getReadOnlyEntity(latest);
        final var model = registry().issuancePolicyConverters(apiVersion()).convert(entity, baseUri);
        populateLifetimeActions(vaultFake, latest, Objects.requireNonNull(model));
        return ResponseEntity.ok(model);
    }

    public ResponseEntity<CertificatePolicyModel> updatePolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            @Valid final CertificatePolicyModel request) {
        log.info("Received request to {} update certificate issuance policy: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        final var vaultFake = getVaultByUri(baseUri);
        final var latest = vaultFake.getEntities()
                .getLatestVersionOfEntity(entityId(baseUri, certificateName));
        CertificateRequestMapperUtil.updateIssuancePolicy(vaultFake, latest, request);
        final var entity = vaultFake.getEntities().getReadOnlyEntity(latest);
        final var model = registry().issuancePolicyConverters(apiVersion()).convert(entity, baseUri);
        populateLifetimeActions(vaultFake, latest, Objects.requireNonNull(model));
        return ResponseEntity.ok(model);
    }
}
