package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {
                CertificateEntityToV73PolicyModelConverter.class,
                CertificateEntityToV73PropertiesModelConverter.class,
        }
)
public abstract class CertificateEntityToV73ModelConverter
        implements RecoveryAwareConverter<ReadOnlyKeyVaultCertificateEntity, KeyVaultCertificateModel, DeletedKeyVaultCertificateModel> {

    @Autowired
    private CertificateEntityToV73PolicyModelConverter certificateEntityToV73PolicyModelConverter;
    @Autowired
    private CertificateEntityToV73PropertiesModelConverter certificateEntityToV73PropertiesModelConverter;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kid", ignore = true)
    @Mapping(target = "sid", ignore = true)
    @Mapping(target = "certificate", source = "source.encodedCertificate")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Override
    public abstract @Nullable KeyVaultCertificateModel convert(@Nullable ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kid", ignore = true)
    @Mapping(target = "sid", ignore = true)
    @Mapping(target = "recoveryId", ignore = true)
    @Mapping(target = "certificate", source = "source.encodedCertificate")
    @Mapping(target = "attributes", ignore = true)
    @Mapping(target = "deletedDate", ignore = true)
    @Mapping(target = "scheduledPurgeDate", ignore = true)
    @Mapping(target = "policy", ignore = true)
    @Override
    public abstract @Nullable DeletedKeyVaultCertificateModel convertDeleted(
            @Nullable ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @AfterMapping
    void postProcess(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri,
            @Nullable @MappingTarget final KeyVaultCertificateModel model) {
        if (source != null && model != null) {
            model.setId(source.getId().asUri(vaultUri).toString());
            model.setKid(source.getKid().asUri(vaultUri).toString());
            model.setSid(source.getSid().asUri(vaultUri).toString());
            model.setAttributes(certificateEntityToV73PropertiesModelConverter.convert(source));
            model.setPolicy(certificateEntityToV73PolicyModelConverter.convert(source, vaultUri));
            if (model instanceof final DeletedKeyVaultCertificateModel deletedModel) {
                deletedModel.setRecoveryId(source.getId().asRecoveryUri(vaultUri).toString());
                deletedModel.setDeletedDate(source.getDeletedDate().orElseThrow());
                deletedModel.setScheduledPurgeDate(source.getScheduledPurgeDate().orElseThrow());
            }
        }
    }

}
