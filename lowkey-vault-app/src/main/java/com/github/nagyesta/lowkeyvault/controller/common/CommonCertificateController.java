package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.createCertificateWithAttributes;
import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.importCertificateWithAttributes;

@Slf4j
public abstract class CommonCertificateController extends BaseCertificateController {

    protected CommonCertificateController(
            @NonNull final CertificateConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    public ResponseEntity<KeyVaultPendingCertificateModel> create(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            @Valid final CreateCertificateRequest request) {
        log.info("Received request to {} create certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = createCertificateWithAttributes(vaultFake, certificateName, request);
        final ReadOnlyKeyVaultCertificateEntity readOnlyEntity = vaultFake.getEntities().getReadOnlyEntity(entityId);
        return ResponseEntity.accepted().body(registry().pendingOperationConverters(apiVersion()).convert(readOnlyEntity, baseUri));
    }


    public ResponseEntity<KeyVaultCertificateModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, certificateName));
    }

    public ResponseEntity<KeyVaultCertificateModel> getWithVersion(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            final URI baseUri) {
        log.info("Received request to {} get certificate: {} with version: {} using API version: {}",
                baseUri.toString(), certificateName, certificateVersion, apiVersion());

        return ResponseEntity.ok(getSpecificEntityModel(baseUri, certificateName, certificateVersion));
    }

    public ResponseEntity<KeyVaultCertificateModel> importCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            @Valid @RequestBody final CertificateImportRequest request) {
        log.info("Received request to {} import certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = importCertificateWithAttributes(vaultFake, certificateName, request);
        return ResponseEntity.ok().body(getSpecificEntityModel(baseUri, certificateName, entityId.version()));
    }

    public ResponseEntity<DeletedKeyVaultCertificateModel> delete(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} delete certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = entityId(baseUri, certificateName);
        vaultFake.delete(entityId);
        final VersionedCertificateEntityId latestVersion = vaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(vaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<DeletedKeyVaultCertificateModel> getDeletedCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get deleted certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = entityId(baseUri, certificateName);
        final VersionedCertificateEntityId latestVersion = vaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(vaultFake, latestVersion, baseUri, false));
    }

    public ResponseEntity<KeyVaultCertificateModel> recoverDeletedCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} recover deleted certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = entityId(baseUri, certificateName);
        vaultFake.recover(entityId);
        final VersionedCertificateEntityId latestVersion = vaultFake.getEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getModelById(vaultFake, latestVersion, baseUri, true));
    }

    public ResponseEntity<Void> purgeDeleted(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} purge deleted certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = entityId(baseUri, certificateName);
        vaultFake.purge(entityId);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> versions(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final int maxResults,
            final int skipToken) {
        log.info("Received request to {} list certificate versions: {} , (max results: {}, skip: {}) using API version: {}",
                baseUri.toString(), certificateName, maxResults, skipToken, apiVersion());

        return ResponseEntity.ok(getPageOfItemVersions(baseUri, certificateName, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/certificates/" + certificateName + "/versions"))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> listCertificates(
            final URI baseUri,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, includePending, apiVersion());

        return ResponseEntity.ok(getPageOfItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion())
                .limit(maxResults)
                .offset(skipToken)
                .base(URI.create(baseUri + "/certificates"))
                .additionalParameters(Map.of(INCLUDE_PENDING_PARAM, String.valueOf(includePending)))
                .build()));
    }

    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel>> listDeletedCertificates(
            final URI baseUri,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list deleted certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, includePending, apiVersion());

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, PaginationContext
                .builder()
                .apiVersion(apiVersion())
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
            @Valid @RequestBody final UpdateCertificateRequest request) {
        log.info("Received request to {} update certificate: {} with version: {} using API version: {}",
                baseUri.toString(), certificateName, certificateVersion, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final VersionedCertificateEntityId entityId = versionedEntityId(baseUri, certificateName, certificateVersion);
        Optional.ofNullable(request.getAttributes())
                .map(CertificatePropertiesModel::isEnabled)
                .ifPresent(enabled -> vaultFake.setEnabled(entityId, enabled));
        vaultFake.clearTags(entityId);
        vaultFake.addTags(entityId, request.getTags());
        final KeyVaultCertificateModel model = getModelById(vaultFake, entityId, baseUri, true);
        model.setPolicy(null);
        return ResponseEntity.ok(model);
    }
}
