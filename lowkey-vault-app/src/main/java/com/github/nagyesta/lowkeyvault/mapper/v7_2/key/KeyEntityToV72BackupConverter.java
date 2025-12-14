package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.Arrays;

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
    @Mapping(target = "tags", expression = "java(java.util.Map.copyOf(source.getTags()))")
    KeyBackupListItem convert(ReadOnlyKeyVaultKeyEntity source);

    default JsonWebKeyImportRequest convertKeyMaterial(final ReadOnlyKeyVaultKeyEntity source) {
        return switch (source.getKeyType()) {
            case RSA, RSA_HSM -> convertKeyMaterial((ReadOnlyRsaKeyVaultKeyEntity) source);
            case EC, EC_HSM -> convertKeyMaterial((ReadOnlyEcKeyVaultKeyEntity) source);
            case OCT, OCT_HSM -> convertKeyMaterial((ReadOnlyAesKeyVaultKeyEntity) source);
        };
    }

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(source.getId().vault()).toString())")
    @Mapping(target = "keyOps", expression = "java(java.util.List.copyOf(source.getOperations()))")
    @Mapping(target = "d", expression = "java(copyOf(source.getD()))")
    @Mapping(target = "dp", expression = "java(copyOf(source.getDp()))")
    @Mapping(target = "dq", expression = "java(copyOf(source.getDq()))")
    @Mapping(target = "e", expression = "java(copyOf(source.getE()))")
    @Mapping(target = "n", expression = "java(copyOf(source.getN()))")
    @Mapping(target = "p", expression = "java(copyOf(source.getP()))")
    @Mapping(target = "q", expression = "java(copyOf(source.getQ()))")
    @Mapping(target = "qi", expression = "java(copyOf(source.getQi()))")
    @Mapping(target = "k", ignore = true)
    @Mapping(target = "x", ignore = true)
    @Mapping(target = "y", ignore = true)
    @Mapping(target = "curveName", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    JsonWebKeyImportRequest convertKeyMaterial(ReadOnlyRsaKeyVaultKeyEntity source);

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(source.getId().vault()).toString())")
    @Mapping(target = "keyOps", expression = "java(java.util.List.copyOf(source.getOperations()))")
    @Mapping(target = "curveName", source = "keyCurveName")
    @Mapping(target = "d", expression = "java(copyOf(source.getD()))")
    @Mapping(target = "x", expression = "java(copyOf(source.getX()))")
    @Mapping(target = "y", expression = "java(copyOf(source.getY()))")
    @Mapping(target = "dp", ignore = true)
    @Mapping(target = "dq", ignore = true)
    @Mapping(target = "e", ignore = true)
    @Mapping(target = "k", ignore = true)
    @Mapping(target = "n", ignore = true)
    @Mapping(target = "p", ignore = true)
    @Mapping(target = "q", ignore = true)
    @Mapping(target = "qi", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    JsonWebKeyImportRequest convertKeyMaterial(ReadOnlyEcKeyVaultKeyEntity source);

    @Named("ignore")
    @Mapping(target = "id", expression = "java(source.getId().asUri(source.getId().vault()).toString())")
    @Mapping(target = "keyOps", expression = "java(java.util.List.copyOf(source.getOperations()))")
    @Mapping(target = "k", expression = "java(copyOf(source.getK()))")
    @Mapping(target = "d", ignore = true)
    @Mapping(target = "dp", ignore = true)
    @Mapping(target = "dq", ignore = true)
    @Mapping(target = "e", ignore = true)
    @Mapping(target = "n", ignore = true)
    @Mapping(target = "p", ignore = true)
    @Mapping(target = "q", ignore = true)
    @Mapping(target = "qi", ignore = true)
    @Mapping(target = "x", ignore = true)
    @Mapping(target = "y", ignore = true)
    @Mapping(target = "curveName", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    JsonWebKeyImportRequest convertKeyMaterial(ReadOnlyAesKeyVaultKeyEntity source);

    @Named("ignore")
    default byte[] copyOf(final byte[] source) {
        return Arrays.copyOf(source, source.length);
    }
}
