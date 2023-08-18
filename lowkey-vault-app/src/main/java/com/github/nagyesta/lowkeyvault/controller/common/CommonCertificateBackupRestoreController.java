package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateLifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.DefaultCertificateLifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil.convertActivityMap;

@Slf4j
public abstract class CommonCertificateBackupRestoreController extends BaseBackupRestoreController<CertificateEntityId,
        VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateModel, DeletedKeyVaultCertificateModel,
        KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel, CertificateEntityToV73ModelConverter,
        CertificateEntityToV73CertificateItemModelConverter, CertificateEntityToV73CertificateVersionItemModelConverter,
        CertificateVaultFake, CertificatePropertiesModel, CertificateBackupListItem, CertificateBackupList, CertificateBackupModel,
        CertificateConverterRegistry> {

    protected CommonCertificateBackupRestoreController(
            @NonNull final CertificateConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService, VaultFake::certificateVaultFake);
    }

    public ResponseEntity<CertificateBackupModel> backup(
            @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            final URI baseUri) {
        log.info("Received request to {} backup certificate: {} using API version: {}",
                baseUri.toString(), certificateName, apiVersion());
        return ResponseEntity.ok(backupEntity(entityId(baseUri, certificateName)));
    }

    public ResponseEntity<KeyVaultCertificateModel> restore(
            final URI baseUri, @Valid final CertificateBackupModel certificateBackupModel) {
        final CertificateBackupList list = certificateBackupModel.getValue();
        log.info("Received request to {} restore certificate: {} using API version: {}",
                baseUri.toString(), list.getVersions().get(0).getId(), apiVersion());
        final KeyVaultCertificateModel model = restoreEntity(certificateBackupModel);
        final CertificateVaultFake vault = getVaultByUri(baseUri);
        final CertificateEntityId entityId = entityId(baseUri, getSingleEntityName(certificateBackupModel));
        model.getPolicy().setLifetimeActions(updateLifetimeActions(vault, entityId, list));
        return ResponseEntity.ok(model);
    }

    @Override
    protected void restoreVersion(@NonNull final CertificateVaultFake vault,
                                  @NonNull final VersionedCertificateEntityId versionedEntityId,
                                  @NonNull final CertificateBackupListItem entityVersion) {
        final CertificatePropertiesModel attributes = Objects
                .requireNonNullElse(entityVersion.getAttributes(), new CertificatePropertiesModel());
        final CertificatePolicyModel issuancePolicy = Optional.ofNullable(entityVersion.getIssuancePolicy())
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
                .created(attributes.getCreatedOn())
                .updated(attributes.getUpdatedOn())
                .notBefore(attributes.getNotBefore())
                .expires(attributes.getExpiresOn())
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
            final CertificateVaultFake vault, final CertificateEntityId entityId, final CertificateBackupList list) {
        final VersionedCertificateEntityId latestVersion = vault.getEntities().getLatestVersionOfEntity(entityId);
        final CertAuthorityType certAuthorityType = vault.getEntities().getReadOnlyEntity(latestVersion)
                .getIssuancePolicy().getCertAuthorityType();
        final CertificateLifetimeActionPolicy lifetimeActionPolicy = Optional.ofNullable(list.getVersions())
                .map(v -> v.get(v.size() - 1))
                .map(CertificateBackupListItem::getPolicy)
                .map(CertificatePolicyModel::getLifetimeActions)
                .map(actions -> new CertificateLifetimeActionPolicy(entityId, convertActivityMap(actions)))
                .orElse(new DefaultCertificateLifetimeActionPolicy(entityId, certAuthorityType));
        vault.setLifetimeActionPolicy(lifetimeActionPolicy);
        return registry().lifetimeActionConverters(apiVersion()).convert(vault.lifetimeActionPolicy(entityId));
    }

}
