package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {SecretEntityToV72PropertiesModelConverter.class}
)
public interface SecretEntityToV72BackupConverter {

    @Mapping(target = "vaultBaseUri", expression = "java(source.getId().vault())")
    @Mapping(target = "id", expression = "java(source.getId().id())")
    @Mapping(target = "version", expression = "java(source.getId().version())")
    @Mapping(target = "attributes", source = "source")
    @Nullable SecretBackupListItem convert(@Nullable ReadOnlyKeyVaultSecretEntity source);

}
