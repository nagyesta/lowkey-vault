package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.createCertificateWithAttributes;
import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.importCertificateWithAttributes;

@Slf4j
public abstract class CommonCertificateController extends GenericEntityController<CertificateEntityId, VersionedCertificateEntityId,
        ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateModel, DeletedKeyVaultCertificateModel, KeyVaultCertificateItemModel,
        DeletedKeyVaultCertificateItemModel, CertificateEntityToV73ModelConverter, CertificateEntityToV73CertificateItemModelConverter,
        CertificateEntityToV73CertificateVersionItemModelConverter, CertificateVaultFake> {

    /**
     * Default parameter value for including the pending certificates.
     */
    protected static final String TRUE = "true";
    /**
     * Parameter name for including the pending certificates.
     */
    protected static final String INCLUDE_PENDING_PARAM = "includePending";
    private final CertificateEntityToV73PendingCertificateOperationModelConverter pendingModelConverter;
    private final LifetimeActionsPolicyToV73ModelConverter lifetimeActionsModelConverter;

    protected CommonCertificateController(
            @NonNull final CertificateEntityToV73ModelConverter modelConverter,
            @NonNull final CertificateEntityToV73CertificateItemModelConverter itemModelConverter,
            @NonNull final CertificateEntityToV73CertificateVersionItemModelConverter versionItemModelConverter,
            @lombok.NonNull final CertificateEntityToV73PendingCertificateOperationModelConverter pendingModelConverter,
            @lombok.NonNull final LifetimeActionsPolicyToV73ModelConverter lifetimeActionsModelConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, itemModelConverter, versionItemModelConverter, vaultService, VaultFake::certificateVaultFake);
        this.pendingModelConverter = pendingModelConverter;
        this.lifetimeActionsModelConverter = lifetimeActionsModelConverter;
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
        return ResponseEntity.accepted().body(pendingModelConverter.convert(readOnlyEntity, baseUri));
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
        return ResponseEntity.ok(pendingModelConverter.convert(readOnlyEntity, baseUri));
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
        return ResponseEntity.ok(pendingModelConverter.convert(readOnlyEntity, baseUri));
    }

    public ResponseEntity<KeyVaultCertificateModel> get(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, certificateName));
    }

    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} get certificate policy: {} with version: -LATEST- using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        return ResponseEntity.ok(getLatestEntityModel(baseUri, certificateName).getPolicy());
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
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
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
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
        final VersionedCertificateEntityId latestVersion = vaultFake.getDeletedEntities().getLatestVersionOfEntity(entityId);
        return ResponseEntity.ok(getDeletedModelById(vaultFake, latestVersion, baseUri, false));
    }

    public ResponseEntity<KeyVaultCertificateModel> recoverDeletedCertificate(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} recover deleted certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());

        final CertificateVaultFake vaultFake = getVaultByUri(baseUri);
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
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
        final CertificateEntityId entityId = new CertificateEntityId(baseUri, certificateName);
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

        return ResponseEntity.ok(getPageOfItemVersions(
                baseUri, certificateName, maxResults, skipToken, "/certificates/" + certificateName + "/versions"));
    }

    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> listCertificates(
            final URI baseUri,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, includePending, apiVersion());

        return ResponseEntity.ok(getPageOfItems(baseUri, maxResults, skipToken, includePending));
    }

    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel>> listDeletedCertificates(
            final URI baseUri,
            final int maxResults,
            final int skipToken,
            final boolean includePending) {
        log.info("Received request to {} list deleted certificates, (max results: {}, skip: {}, includePending: {}) using API version: {}",
                baseUri.toString(), maxResults, skipToken, includePending, apiVersion());

        return ResponseEntity.ok(getPageOfDeletedItems(baseUri, maxResults, skipToken, includePending));
    }

    @Override
    protected KeyVaultCertificateModel getModelById(
            final CertificateVaultFake entityVaultFake,
            final VersionedCertificateEntityId entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final KeyVaultCertificateModel model = super.getModelById(entityVaultFake, entityId, baseUri, includeDisabled);
        setLifetimeActionModels(entityId, baseUri, model.getPolicy()::setLifetimeActions);
        return model;
    }

    @Override
    protected DeletedKeyVaultCertificateModel getDeletedModelById(
            final CertificateVaultFake entityVaultFake,
            final VersionedCertificateEntityId entityId,
            final URI baseUri,
            final boolean includeDisabled) {
        final DeletedKeyVaultCertificateModel model = super.getDeletedModelById(entityVaultFake, entityId, baseUri, includeDisabled);
        setLifetimeActionModels(entityId, baseUri, model.getPolicy()::setLifetimeActions);
        return model;
    }

    private void setLifetimeActionModels(
            final VersionedCertificateEntityId entityId,
            final URI baseUri,
            final Consumer<List<CertificateLifetimeActionModel>> consumer) {
        Optional.ofNullable(getVaultByUri(baseUri).lifetimeActionPolicy(entityId))
                .map(lifetimeActionsModelConverter::convert)
                .ifPresent(consumer);
    }

    private KeyVaultItemListModel<KeyVaultCertificateItemModel> getPageOfItems(
            final URI baseUri, final int limit, final int offset, final boolean includePending) {
        final KeyVaultItemListModel<KeyVaultCertificateItemModel> page =
                super.getPageOfItems(baseUri, limit, offset, "/certificates");
        return fixNextLink(page, includePending);
    }

    private KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel> getPageOfDeletedItems(
            final URI baseUri, final int limit, final int offset, final boolean includePending) {
        final KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel> page =
                super.getPageOfDeletedItems(baseUri, limit, offset, "/deletedcertificates");
        return fixNextLink(page, includePending);
    }

    @Override
    protected VersionedCertificateEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedCertificateEntityId(baseUri, name, version);
    }

    @Override
    protected CertificateEntityId entityId(final URI baseUri, final String name) {
        return new CertificateEntityId(baseUri, name);
    }

    private <LI> KeyVaultItemListModel<LI> fixNextLink(
            final KeyVaultItemListModel<LI> page,
            final boolean includePending) {
        final String nextLink = Optional.ofNullable(page.getNextLink())
                .map(next -> next + "&" + INCLUDE_PENDING_PARAM + "=" + includePending)
                .orElse(null);
        page.setNextLink(nextLink);
        return page;
    }
}
