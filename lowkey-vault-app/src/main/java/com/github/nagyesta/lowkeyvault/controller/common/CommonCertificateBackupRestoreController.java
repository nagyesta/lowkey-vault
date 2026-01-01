package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateLifetimeActionsPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateLifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.DefaultCertificateLifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.convertActivityMap;

@Slf4j
public abstract class CommonCertificateBackupRestoreController
        extends BaseBackupRestoreController<CertificateEntityId, VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity,
        KeyVaultCertificateModel, DeletedKeyVaultCertificateModel, KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel,
        CertificateVaultFake, CertificatePropertiesModel, CertificateBackupListItem, CertificateBackupList, CertificateBackupModel> {

    private final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter;

    protected CommonCertificateBackupRestoreController(
            final VaultService vaultService,
            final CertificateEntityToV73ModelConverter modelConverter,
            final CertificateEntityToV73CertificateItemModelConverter itemConverter,
            final CertificateEntityToV73BackupConverter backupConverter,
            final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter) {
        super(vaultService, modelConverter, itemConverter, VaultFake::certificateVaultFake, backupConverter::convert);
        this.lifetimeActionConverter = lifetimeActionConverter;
    }

    public ResponseEntity<CertificateBackupModel> backup(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri,
            final String apiVersion) {
        log.info("Received request to {} backup certificate: {} using API version: {}",
                baseUri, certificateName, apiVersion);
        return ResponseEntity.ok(backupEntity(entityId(baseUri, certificateName)));
    }

    public ResponseEntity<KeyVaultCertificateModel> restore(
            final URI baseUri,
            final String apiVersion,
            @Valid final CertificateBackupModel certificateBackupModel) {
        final var list = Objects.requireNonNull(certificateBackupModel.getValue());
        log.info("Received request to {} restore certificate: {} using API version: {}",
                baseUri, list.getVersions().getFirst().getId(), apiVersion);
        final var model = restoreEntity(certificateBackupModel);
        final var vault = getVaultByUri(baseUri);
        final var entityId = entityId(baseUri, getSingleEntityName(certificateBackupModel));
        final var policy = Objects.requireNonNull(model.getPolicy());
        policy.setLifetimeActions(updateLifetimeActions(vault, entityId, list));
        return ResponseEntity.ok(model);
    }

    @Override
    protected void restoreVersion(
            final CertificateVaultFake vault,
            final VersionedCertificateEntityId versionedEntityId,
            final CertificateBackupListItem entityVersion) {
        final var attributes = Objects
                .requireNonNullElse(entityVersion.getAttributes(), new CertificatePropertiesModel());
        final var issuancePolicy = Optional.ofNullable(entityVersion.getIssuancePolicy())
                .orElse(entityVersion.getPolicy());
        vault.restoreCertificateVersion(versionedEntityId, CertificateRestoreInput.builder()
                .name(versionedEntityId.id())
                .certificateContent(entityVersion.getCertificateAsString())
                .keyVersion(entityVersion.getKeyVersion())
                .contentType(CertContentType.byMimeType(entityVersion.getPolicy().getSecretProperties().getContentType()))
                .password(entityVersion.getPassword())
                .policy(entityVersion.getPolicy())
                .issuancePolicy(issuancePolicy)
                .tags(entityVersion.getTags())
                .created(attributes.getCreated())
                .updated(attributes.getUpdated())
                .notBefore(attributes.getNotBefore())
                .expires(attributes.getExpiry())
                .enabled(attributes.isEnabled())
                .build());
    }

    @Override
    protected CertificateBackupList getBackupList() {
        return new CertificateBackupList();
    }

    @Override
    protected CertificateBackupModel getBackupModel() {
        return new CertificateBackupModel();
    }

    private List<CertificateLifetimeActionModel> updateLifetimeActions(
            final CertificateVaultFake vault,
            final CertificateEntityId entityId,
            final CertificateBackupList list) {
        final var latestVersion = vault.getEntities().getLatestVersionOfEntity(entityId);
        final var certAuthorityType = vault.getEntities().getReadOnlyEntity(latestVersion)
                .getIssuancePolicy().getCertAuthorityType();
        final var lifetimeActionPolicy = Optional.of(list.getVersions())
                .map(List::getLast)
                .map(CertificateBackupListItem::getPolicy)
                .map(CertificatePolicyModel::getLifetimeActions)
                .map(actions -> new CertificateLifetimeActionPolicy(entityId, convertActivityMap(actions)))
                .orElse(new DefaultCertificateLifetimeActionPolicy(entityId, certAuthorityType));
        vault.setLifetimeActionPolicy(lifetimeActionPolicy);
        final var saved = vault.lifetimeActionPolicy(entityId);
        final var lifetimeActionModels = lifetimeActionConverter.convert(saved);
        return Objects.requireNonNull(lifetimeActionModels);
    }

    @Override
    protected CertificateEntityId entityId(
            final URI baseUri,
            final String name) {
        return new CertificateEntityId(baseUri, name);
    }

    @Override
    protected VersionedCertificateEntityId versionedEntityId(
            final URI baseUri,
            final String name,
            final String version) {
        return new VersionedCertificateEntityId(baseUri, name, version);
    }
}
