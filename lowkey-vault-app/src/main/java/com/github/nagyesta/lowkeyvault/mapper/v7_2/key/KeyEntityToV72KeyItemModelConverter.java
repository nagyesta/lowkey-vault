package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {KeyEntityToV72PropertiesModelConverter.class}
)
public interface KeyEntityToV72KeyItemModelConverter
        extends RecoveryAwareItemConverter<ReadOnlyKeyVaultKeyEntity, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel> {

    @Override
    default @Nullable KeyVaultKeyItemModel convert(
            @Nullable final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvert(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "keyId", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyVaultKeyItemModel doConvert(ReadOnlyKeyVaultKeyEntity source, URI vaultUri);

    @Override
    default @Nullable KeyVaultKeyItemModel convertWithoutVersion(
            @Nullable final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertWithoutVersion(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "keyId", expression = "java(source.getId().asUriNoVersion(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyVaultKeyItemModel doConvertWithoutVersion(ReadOnlyKeyVaultKeyEntity source, URI vaultUri);

    @Override
    default @Nullable DeletedKeyVaultKeyItemModel convertDeleted(
            @Nullable final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertDeleted(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "keyId", expression = "java(source.getId().asUriNoVersion(vaultUri).toString())")
    @Mapping(target = "recoveryId", expression = "java(source.getId().asRecoveryUri(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", expression = "java(source.getDeletedDate().orElseThrow())")
    @Mapping(target = "scheduledPurgeDate", expression = "java(source.getScheduledPurgeDate().orElseThrow())")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    DeletedKeyVaultKeyItemModel doConvertDeleted(ReadOnlyKeyVaultKeyEntity source, URI vaultUri);

}
