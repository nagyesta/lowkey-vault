package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {KeyEntityToV72PropertiesModelConverter.class}
)
public interface KeyEntityToV72BackupConverter {

    @Mapping(target = "vaultBaseUri", expression = "java(source.getId().vault())")
    @Mapping(target = "id", expression = "java(source.getId().id())")
    @Mapping(target = "version", expression = "java(source.getId().version())")
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "keyMaterial", expression = "java(convertKeyMaterial(source))")
    @Nullable KeyBackupListItem convert(@Nullable ReadOnlyKeyVaultKeyEntity source);

    default JsonWebKeyImportRequest convertKeyMaterial(final ReadOnlyKeyVaultKeyEntity source) {
        return switch (source.getKeyType()) {
            case RSA, RSA_HSM -> convertKeyMaterial((ReadOnlyRsaKeyVaultKeyEntity) source);
            case EC, EC_HSM -> convertKeyMaterial((ReadOnlyEcKeyVaultKeyEntity) source);
            case OCT, OCT_HSM -> convertKeyMaterial((ReadOnlyAesKeyVaultKeyEntity) source);
        };
    }

    @Mapping(target = "id", expression = "java(source.getId().asUri(source.getId().vault()).toString())")
    @Mapping(target = "keyOps", source = "operations")
    @Mapping(target = "curveName", ignore = true)
    @Mapping(target = "k", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    @Mapping(target = "x", ignore = true)
    @Mapping(target = "y", ignore = true)
    JsonWebKeyImportRequest convertKeyMaterial(ReadOnlyRsaKeyVaultKeyEntity source);

    @Mapping(target = "id", expression = "java(source.getId().asUri(source.getId().vault()).toString())")
    @Mapping(target = "keyOps", source = "operations")
    @Mapping(target = "curveName", source = "keyCurveName")
    @Mapping(target = "dp", ignore = true)
    @Mapping(target = "dq", ignore = true)
    @Mapping(target = "e", ignore = true)
    @Mapping(target = "k", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    @Mapping(target = "n", ignore = true)
    @Mapping(target = "p", ignore = true)
    @Mapping(target = "q", ignore = true)
    @Mapping(target = "qi", ignore = true)
    JsonWebKeyImportRequest convertKeyMaterial(ReadOnlyEcKeyVaultKeyEntity source);

    @Mapping(target = "id", expression = "java(source.getId().asUri(source.getId().vault()).toString())")
    @Mapping(target = "keyOps", source = "operations")
    @Mapping(target = "curveName", ignore = true)
    @Mapping(target = "d", ignore = true)
    @Mapping(target = "dp", ignore = true)
    @Mapping(target = "dq", ignore = true)
    @Mapping(target = "e", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    @Mapping(target = "n", ignore = true)
    @Mapping(target = "p", ignore = true)
    @Mapping(target = "q", ignore = true)
    @Mapping(target = "qi", ignore = true)
    @Mapping(target = "x", ignore = true)
    @Mapping(target = "y", ignore = true)
    JsonWebKeyImportRequest convertKeyMaterial(ReadOnlyAesKeyVaultKeyEntity source);
}
