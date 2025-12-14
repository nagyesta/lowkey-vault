package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareItemConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SecretEntityToV72PropertiesModelConverter.class}
)
public interface SecretEntityToV72SecretItemModelConverter
        extends RecoveryAwareItemConverter<ReadOnlyKeyVaultSecretEntity, KeyVaultSecretItemModel, DeletedKeyVaultSecretItemModel> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Override
    @Nullable KeyVaultSecretItemModel convert(@Nullable ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

    @Override
    default @Nullable KeyVaultSecretItemModel convertWithoutVersion(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri) {
        final var model = convert(source, vaultUri);
        if (source != null && model != null) {
            model.setId(source.getId().asUriNoVersion(vaultUri).toString());
        }
        return model;
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recoveryId", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", ignore = true)
    @Mapping(target = "scheduledPurgeDate", ignore = true)
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Override
    @Nullable DeletedKeyVaultSecretItemModel convertDeleted(@Nullable ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

    @AfterMapping
    default void postProcess(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri,
            @Nullable @MappingTarget final KeyVaultSecretItemModel model) {
        if (source != null && model != null) {
            if (model instanceof final DeletedKeyVaultSecretItemModel deletedModel) {
                deletedModel.setId(source.getId().asUriNoVersion(vaultUri).toString());
                deletedModel.setRecoveryId(source.getId().asRecoveryUri(vaultUri).toString());
                deletedModel.setDeletedDate(source.getDeletedDate().orElseThrow());
                deletedModel.setScheduledPurgeDate(source.getScheduledPurgeDate().orElseThrow());
            } else {
                model.setId(source.getId().asUri(vaultUri).toString());
            }
        }
    }

}
