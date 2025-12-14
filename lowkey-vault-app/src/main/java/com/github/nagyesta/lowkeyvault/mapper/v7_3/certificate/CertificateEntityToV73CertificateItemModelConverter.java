package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.DeletedKeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateItemModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {CertificateEntityToV73PropertiesModelConverter.class}
)
public interface CertificateEntityToV73CertificateItemModelConverter
        extends RecoveryAwareItemConverter<ReadOnlyKeyVaultCertificateEntity,
        KeyVaultCertificateItemModel, DeletedKeyVaultCertificateItemModel> {

    @Mapping(target = "certificateId", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Override
    @Nullable KeyVaultCertificateItemModel convert(@Nullable ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @Override
    @Nullable
    default KeyVaultCertificateItemModel convertWithoutVersion(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri) {
        final var model = convert(source, vaultUri);
        if (source != null && model != null) {
            model.setCertificateId(source.getId().asUriNoVersion(vaultUri).toString());
        }
        return model;
    }

    @Mapping(target = "certificateId", ignore = true)
    @Mapping(target = "recoveryId", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", ignore = true)
    @Mapping(target = "scheduledPurgeDate", ignore = true)
    @Override
    @Nullable DeletedKeyVaultCertificateItemModel convertDeleted(@Nullable ReadOnlyKeyVaultCertificateEntity source, URI vaultUri);

    @AfterMapping
    default void postProcess(
            @Nullable final ReadOnlyKeyVaultCertificateEntity source,
            final URI vaultUri,
            @Nullable @MappingTarget final KeyVaultCertificateItemModel model) {
        if (source != null && model != null) {
            if (model instanceof final DeletedKeyVaultCertificateItemModel deletedModel) {
                deletedModel.setCertificateId(source.getId().asUriNoVersion(vaultUri).toString());
                deletedModel.setRecoveryId(source.getId().asRecoveryUri(vaultUri).toString());
                deletedModel.setDeletedDate(source.getDeletedDate().orElseThrow());
                deletedModel.setScheduledPurgeDate(source.getScheduledPurgeDate().orElseThrow());
            } else {
                model.setCertificateId(source.getId().asUri(vaultUri).toString());
            }
        }
    }
}
