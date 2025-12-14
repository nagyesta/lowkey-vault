package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {KeyEntityToV72PropertiesModelConverter.class}
)
public interface KeyEntityToV72KeyItemModelConverter
        extends RecoveryAwareItemConverter<ReadOnlyKeyVaultKeyEntity, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel> {

    @Mapping(target = "keyId", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Override
    @Nullable KeyVaultKeyItemModel convert(@Nullable ReadOnlyKeyVaultKeyEntity source, URI vaultUri);

    @Override
    default @Nullable KeyVaultKeyItemModel convertWithoutVersion(
            @Nullable final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri) {
        final var model = convert(source, vaultUri);
        if (source != null && model != null) {
            model.setKeyId(source.getId().asUriNoVersion(vaultUri).toString());
        }
        return model;
    }

    @Mapping(target = "keyId", ignore = true)
    @Mapping(target = "recoveryId", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", ignore = true)
    @Mapping(target = "scheduledPurgeDate", ignore = true)
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Override
    @Nullable DeletedKeyVaultKeyItemModel convertDeleted(@Nullable ReadOnlyKeyVaultKeyEntity source, URI vaultUri);

    @AfterMapping
    default void postProcess(
            @Nullable final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri,
            @Nullable @MappingTarget final KeyVaultKeyItemModel model) {
        if (source != null && model != null) {
            if (model instanceof final DeletedKeyVaultKeyItemModel deletedModel) {
                deletedModel.setKeyId(source.getId().asUriNoVersion(vaultUri).toString());
                deletedModel.setRecoveryId(source.getId().asRecoveryUri(vaultUri).toString());
                deletedModel.setDeletedDate(source.getDeletedDate().orElseThrow());
                deletedModel.setScheduledPurgeDate(source.getScheduledPurgeDate().orElseThrow());
            } else {
                model.setKeyId(source.getId().asUri(vaultUri).toString());
            }
        }
    }

}
