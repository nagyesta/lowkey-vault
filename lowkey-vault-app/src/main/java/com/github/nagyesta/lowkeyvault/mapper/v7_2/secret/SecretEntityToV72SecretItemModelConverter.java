package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SecretEntityToV72PropertiesModelConverter.class}
)
public interface SecretEntityToV72SecretItemModelConverter
        extends RecoveryAwareItemConverter<ReadOnlyKeyVaultSecretEntity, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel> {

    @Override
    default @Nullable KeyVaultSecretItemModel convert(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvert(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "contentType", expression = "java(source.getContentType())")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyVaultSecretItemModel doConvert(ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

    @Override
    default @Nullable KeyVaultSecretItemModel convertWithoutVersion(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertWithoutVersion(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUriNoVersion(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "contentType", expression = "java(source.getContentType())")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyVaultSecretItemModel doConvertWithoutVersion(ReadOnlyKeyVaultSecretEntity source, URI vaultUri);


    @Override
    default @Nullable DeletedKeyVaultSecretItemModel convertDeleted(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertDeleted(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUriNoVersion(vaultUri).toString())")
    @Mapping(target = "recoveryId", expression = "java(source.getId().asRecoveryUri(vaultUri).toString())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "contentType", expression = "java(source.getContentType())")
    @Mapping(target = "deletedDate", expression = "java(source.getDeletedDate().orElseThrow())")
    @Mapping(target = "scheduledPurgeDate", expression = "java(source.getScheduledPurgeDate().orElseThrow())")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    DeletedKeyVaultSecretItemModel doConvertDeleted(ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

}
