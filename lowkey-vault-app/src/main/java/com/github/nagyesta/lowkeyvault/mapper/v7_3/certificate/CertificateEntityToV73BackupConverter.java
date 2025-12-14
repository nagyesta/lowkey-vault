package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAsymmetricKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class CertificateEntityToV73BackupConverter {

    private final VaultService vaultService;
    private final CertificateEntityToV73PolicyModelConverter policyModelConverter;
    private final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyModelConverter;
    private final CertificateEntityToV73PropertiesModelConverter propertiesModelConverter;
    private final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionsConverter;

    public CertificateEntityToV73BackupConverter(
            final VaultService vaultService,
            final CertificateEntityToV73PolicyModelConverter policyModelConverter,
            final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyModelConverter,
            final CertificateEntityToV73PropertiesModelConverter propertiesModelConverter,
            final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionsConverter) {
        this.vaultService = vaultService;
        this.policyModelConverter = policyModelConverter;
        this.issuancePolicyModelConverter = issuancePolicyModelConverter;
        this.propertiesModelConverter = propertiesModelConverter;
        this.lifetimeActionsConverter = lifetimeActionsConverter;
    }

    public CertificateBackupListItem convert(final ReadOnlyKeyVaultCertificateEntity source) {
        final var listItem = new CertificateBackupListItem();
        final var vaultUri = source.getId().vault();
        final var vaultFake = vaultService.findByUri(vaultUri);
        final var key = vaultFake
                .keyVaultFake()
                .getEntities()
                .getEntity(source.getKid(), ReadOnlyAsymmetricKeyVaultKeyEntity.class);
        listItem.setVaultBaseUri(vaultUri);
        listItem.setId(source.getId().id());
        listItem.setVersion(Objects.requireNonNull(source.getId().version()));
        listItem.setKeyVersion(source.getKid().version());
        listItem.setPassword(CertContentType.BACKUP_PASSWORD);
        final var certificateBytes = source.getOriginalCertificatePolicy().getContentType()
                .certificatePackageForBackup(source.getCertificate(), key.getKey());
        listItem.setCertificate(certificateBytes);
        listItem.setPolicy(Objects.requireNonNull(policyModelConverter.convert(source, vaultUri)));
        final var issuancePolicy = issuancePolicyModelConverter.convert(source, vaultUri);
        final var lifetimeActionModels = fetchLifetimeActionModels(source, vaultFake);
        listItem.getPolicy().setLifetimeActions(lifetimeActionModels);
        if (issuancePolicy != null) {
            issuancePolicy.setLifetimeActions(lifetimeActionModels);
        }
        listItem.setIssuancePolicy(issuancePolicy);
        listItem.setAttributes(Objects.requireNonNull(propertiesModelConverter.convert(source)));
        listItem.setTags(source.getTags());
        listItem.setManaged(false);
        return listItem;
    }

    private List<CertificateLifetimeActionModel> fetchLifetimeActionModels(
            final ReadOnlyKeyVaultCertificateEntity source,
            final VaultFake vaultFake) {
        final var lifetimeActionPolicy = vaultFake.certificateVaultFake().lifetimeActionPolicy(source.getId());
        return Objects.requireNonNull(lifetimeActionsConverter.convert(lifetimeActionPolicy));
    }
}
