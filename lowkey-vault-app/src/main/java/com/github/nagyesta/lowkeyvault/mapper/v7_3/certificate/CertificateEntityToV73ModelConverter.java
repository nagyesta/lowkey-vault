package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
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

    @Override
    public @Nullable KeyVaultCertificateModel convert(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvert(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "kid", expression = "java(source.getKid().asUri(vaultUri).toString())")
    @Mapping(target = "sid", expression = "java(source.getSid().asUri(vaultUri).toString())")
    @Mapping(target = "certificate", source = "source.encodedCertificate")
    @Mapping(target = "attributes", expression = "java(convertAttributes(source))")
    @Mapping(target = "policy", expression = "java(convertPolicy(source, vaultUri))")
    public abstract KeyVaultCertificateModel doConvert(ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);


    @Override
    public @Nullable DeletedKeyVaultCertificateModel convertDeleted(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertDeleted(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "kid", expression = "java(source.getKid().asUri(vaultUri).toString())")
    @Mapping(target = "sid", expression = "java(source.getSid().asUri(vaultUri).toString())")
    @Mapping(target = "recoveryId", expression = "java(source.getId().asRecoveryUri(vaultUri).toString())")
    @Mapping(target = "certificate", source = "source.encodedCertificate")
    @Mapping(target = "attributes", expression = "java(convertAttributes(source))")
    @Mapping(target = "policy", expression = "java(convertPolicy(source, vaultUri))")
    @Mapping(target = "deletedDate", expression = "java(source.getDeletedDate().orElseThrow())")
    @Mapping(target = "scheduledPurgeDate", expression = "java(source.getScheduledPurgeDate().orElseThrow())")
    public abstract DeletedKeyVaultCertificateModel doConvertDeleted(ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    protected @Nullable CertificatePropertiesModel convertAttributes(final ReadOnlyKeyVaultCertificateEntity source) {
        return certificateEntityToV73PropertiesModelConverter.convert(source);
    }
    protected @Nullable CertificatePolicyModel convertPolicy(
            final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        return certificateEntityToV73PolicyModelConverter.convert(source, vaultUri);
    }
}
