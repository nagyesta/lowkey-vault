package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.net.URI;
import java.util.Arrays;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CertificateEntityToV73PropertiesModelConverter.class}
)
public interface CertificateEntityToV73CertificateItemModelConverter
        extends RecoveryAwareItemConverter<ReadOnlyKeyVaultCertificateEntity,
        KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel> {

    @Override
    default @Nullable KeyVaultCertificateItemModel convert(
            final @Nullable ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvert(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "certificateId", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "thumbprint", expression = "java(copyOf(source.getThumbprint()))")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyVaultCertificateItemModel doConvert(ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @Override
    default @Nullable KeyVaultCertificateItemModel convertWithoutVersion(
            final @Nullable ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertWithoutVersion(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "certificateId", expression = "java(source.getId().asUriNoVersion(vaultUri).toString())")
    @Mapping(target = "thumbprint", expression = "java(copyOf(source.getThumbprint()))")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyVaultCertificateItemModel doConvertWithoutVersion(ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @Override
    default @Nullable DeletedKeyVaultCertificateItemModel convertDeleted(
            final @Nullable ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertDeleted(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "certificateId", expression = "java(source.getId().asUriNoVersion(vaultUri).toString())")
    @Mapping(target = "recoveryId", expression = "java(source.getId().asRecoveryUri(vaultUri).toString())")
    @Mapping(target = "thumbprint", expression = "java(copyOf(source.getThumbprint()))")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", expression = "java(source.getDeletedDate().orElseThrow())")
    @Mapping(target = "scheduledPurgeDate", expression = "java(source.getScheduledPurgeDate().orElseThrow())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    DeletedKeyVaultCertificateItemModel doConvertDeleted(ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @Named("ignore")
    default byte[] copyOf(final byte[] source) {
        return Arrays.copyOf(source, source.length);
    }
}
