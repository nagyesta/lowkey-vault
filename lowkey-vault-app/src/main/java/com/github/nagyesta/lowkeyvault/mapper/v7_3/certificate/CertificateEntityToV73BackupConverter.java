package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAsymmetricKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.SortedSet;

public class CertificateEntityToV73BackupConverter
        extends BackupConverter<CertificateEntityId, VersionedCertificateEntityId, ReadOnlyKeyVaultCertificateEntity,
        CertificatePropertiesModel, CertificateBackupListItem> {

    private final CertificateConverterRegistry registry;
    private final VaultService vaultService;

    @Autowired
    public CertificateEntityToV73BackupConverter(
            @lombok.NonNull final CertificateConverterRegistry registry,
            @lombok.NonNull final VaultService vaultService) {
        this.registry = registry;
        this.vaultService = vaultService;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerBackupConverter(this);
    }

    @Override
    protected CertificateBackupListItem convertUniqueFields(@NonNull final ReadOnlyKeyVaultCertificateEntity source) {
        final var listItem = new CertificateBackupListItem();
        final var vaultUri = source.getId().vault();
        final var vaultFake = vaultService.findByUri(vaultUri);
        final var key = vaultFake
                .keyVaultFake()
                .getEntities()
                .getEntity(source.getKid(), ReadOnlyAsymmetricKeyVaultKeyEntity.class);
        listItem.setVaultBaseUri(vaultUri);
        listItem.setId(source.getId().id());
        listItem.setVersion(source.getId().version());
        listItem.setKeyVersion(source.getKid().version());
        listItem.setPassword(CertContentType.BACKUP_PASSWORD);
        final var certificateBytes = source.getOriginalCertificatePolicy().getContentType()
                .certificatePackageForBackup(source.getCertificate(), key.getKey());
        listItem.setCertificate(certificateBytes);
        listItem.setPolicy(registry.policyConverters(supportedVersions().last()).convert(source, vaultUri));
        listItem.setIssuancePolicy(registry.issuancePolicyConverters(supportedVersions().last()).convert(source, vaultUri));
        final var lifetimeActionModels = fetchLifetimeActionModels(source, vaultFake);
        listItem.getPolicy().setLifetimeActions(lifetimeActionModels);
        listItem.getIssuancePolicy().setLifetimeActions(lifetimeActionModels);
        listItem.setAttributes(registry.propertiesConverter(supportedVersions().last()).convert(source, vaultUri));
        listItem.setTags(source.getTags());
        listItem.setManaged(false);
        return listItem;
    }

    private List<CertificateLifetimeActionModel> fetchLifetimeActionModels(
            final ReadOnlyKeyVaultCertificateEntity source, final VaultFake vaultFake) {
        final var lifetimeActionPolicy = vaultFake.certificateVaultFake().lifetimeActionPolicy(source.getId());
        return registry.lifetimeActionConverters(supportedVersions().last()).convert(lifetimeActionPolicy);
    }

    @Override
    protected AliasAwareConverter<ReadOnlyKeyVaultCertificateEntity, CertificatePropertiesModel> propertiesConverter() {
        return registry.propertiesConverter(supportedVersions().last());
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_3_AND_LATER;
    }
}
