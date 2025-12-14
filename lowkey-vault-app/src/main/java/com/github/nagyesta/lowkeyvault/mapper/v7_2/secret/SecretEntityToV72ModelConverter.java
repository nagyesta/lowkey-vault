package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.net.URI;
import java.util.Objects;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SecretEntityToV72PropertiesModelConverter.class}
)
public interface SecretEntityToV72ModelConverter
        extends RecoveryAwareConverter<ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel, DeletedKeyVaultSecretModel> {

    @Override
    default @Nullable KeyVaultSecretModel convert(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvert(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "kid", expression = "java(convertKeyId(source, vaultUri))")
    @Mapping(target = "value", expression = "java(source.getValue())")
    @Mapping(target = "contentType", expression = "java(source.getContentType())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyVaultSecretModel doConvert(ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

    @Override
    default @Nullable DeletedKeyVaultSecretModel convertDeleted(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        if (source == null) {
            return null;
        }
        return doConvertDeleted(source, vaultUri);
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "kid", expression = "java(convertKeyId(source, vaultUri))")
    @Mapping(target = "recoveryId", expression = "java(source.getId().asRecoveryUri(vaultUri).toString())")
    @Mapping(target = "value", expression = "java(source.getValue())")
    @Mapping(target = "contentType", expression = "java(source.getContentType())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", expression = "java(source.getDeletedDate().orElseThrow())")
    @Mapping(target = "scheduledPurgeDate", expression = "java(source.getScheduledPurgeDate().orElseThrow())")
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    DeletedKeyVaultSecretModel doConvertDeleted(ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

    @Named("ignore")
    default @Nullable String convertKeyId(
            final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        if (!source.isManaged()) {
            return null;
        }
        return new VersionedKeyEntityId(vaultUri, source.getId().id(), Objects.requireNonNull(source.getId().version()))
                .asUri(vaultUri).toString();
    }
}
