package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SecretEntityToV72PropertiesModelConverter.class}
)
public interface SecretEntityToV72ModelConverter
        extends RecoveryAwareConverter<ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel, DeletedKeyVaultSecretModel> {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kid", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Override
    @Nullable KeyVaultSecretModel convert(@Nullable ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "kid", ignore = true)
    @Mapping(target = "recoveryId", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", ignore = true)
    @Mapping(target = "scheduledPurgeDate", ignore = true)
    @Override
    @Nullable DeletedKeyVaultSecretModel convertDeleted(@Nullable ReadOnlyKeyVaultSecretEntity source, URI vaultUri);

    @AfterMapping
    default void postProcess(
            @Nullable final ReadOnlyKeyVaultSecretEntity source,
            final URI vaultUri,
            @Nullable @MappingTarget final KeyVaultSecretModel model) {
        if (source != null && model != null) {
            final var sourceId = source.getId();
            model.setId(source.getId().asUri(vaultUri).toString());
            if (source.isManaged()) {
                model.setKid(new VersionedKeyEntityId(vaultUri, sourceId.id(), sourceId.version()).asUri(vaultUri).toString());
            }
            if (model instanceof final DeletedKeyVaultSecretModel deletedModel) {
                deletedModel.setRecoveryId(source.getId().asRecoveryUri(vaultUri).toString());
                deletedModel.setDeletedDate(source.getDeletedDate().orElseThrow());
                deletedModel.setScheduledPurgeDate(source.getScheduledPurgeDate().orElseThrow());
            }
        }
    }
}
