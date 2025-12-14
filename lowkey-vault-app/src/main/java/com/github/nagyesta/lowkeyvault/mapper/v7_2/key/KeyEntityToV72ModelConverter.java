package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.RecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.JsonWebKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import org.jspecify.annotations.Nullable;
import org.mapstruct.*;

import java.net.URI;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {KeyEntityToV72PropertiesModelConverter.class}
)
public interface KeyEntityToV72ModelConverter
        extends RecoveryAwareConverter<ReadOnlyKeyVaultKeyEntity, KeyVaultKeyModel, DeletedKeyVaultKeyModel> {


    @Mapping(target = "key", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Override
    @Nullable KeyVaultKeyModel convert(ReadOnlyKeyVaultKeyEntity source, URI vaultUri);

    @Mapping(target = "key", ignore = true)
    @Mapping(target = "recoveryId", ignore = true)
    @Mapping(target = "attributes", source = "source")
    @Mapping(target = "deletedDate", ignore = true)
    @Mapping(target = "scheduledPurgeDate", ignore = true)
    @Mapping(target = "managed", source = "source.managed", conditionExpression = "java(source.isManaged())")
    @Override
    @Nullable DeletedKeyVaultKeyModel convertDeleted(@Nullable  ReadOnlyKeyVaultKeyEntity source, URI vaultUri);

    @AfterMapping
    default void postProcess(
            @Nullable final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri,
            @Nullable @MappingTarget final KeyVaultKeyModel model) {
        if (source != null && model != null) {
            if (model instanceof final DeletedKeyVaultKeyModel deletedModel) {
                deletedModel.setKey(convertJsonWebKey(source, vaultUri));
                deletedModel.setRecoveryId(source.getId().asRecoveryUri(vaultUri).toString());
                deletedModel.setDeletedDate(source.getDeletedDate().orElseThrow());
                deletedModel.setScheduledPurgeDate(source.getScheduledPurgeDate().orElseThrow());
            } else {
                model.setKey(convertJsonWebKey(source, vaultUri));
            }
        }
    }

    default JsonWebKeyModel convertJsonWebKey(
            final ReadOnlyKeyVaultKeyEntity source,
            final URI vaultUri) {
        return switch (source.getKeyType()) {
            case RSA, RSA_HSM -> convertJsonWebKey((ReadOnlyRsaKeyVaultKeyEntity) source, vaultUri);
            case EC, EC_HSM -> convertJsonWebKey((ReadOnlyEcKeyVaultKeyEntity) source, vaultUri);
            case OCT, OCT_HSM -> convertJsonWebKey((ReadOnlyAesKeyVaultKeyEntity) source, vaultUri);
        };
    }

    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "keyOps", source = "source.operations")
    @Mapping(target = "curveName", ignore = true)
    @Mapping(target = "k", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    @Mapping(target = "x", ignore = true)
    @Mapping(target = "y", ignore = true)
    @Mapping(target = "d", ignore = true) //Do not return the private key.
    @Mapping(target = "dp", ignore = true) //Do not return the private key.
    @Mapping(target = "dq", ignore = true) //Do not return the private key.
    @Mapping(target = "p", ignore = true) //Do not return the private key.
    @Mapping(target = "q", ignore = true) //Do not return the private key.
    @Mapping(target = "qi", ignore = true) //Do not return the private key.
    JsonWebKeyModel convertJsonWebKey(ReadOnlyRsaKeyVaultKeyEntity source, URI vaultUri);

    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "keyOps", source = "source.operations")
    @Mapping(target = "curveName", source = "source.keyCurveName")
    @Mapping(target = "d", ignore = true) //Do not return the private key.
    @Mapping(target = "dp", ignore = true)
    @Mapping(target = "dq", ignore = true)
    @Mapping(target = "e", ignore = true)
    @Mapping(target = "k", ignore = true)
    @Mapping(target = "keyHsm", ignore = true)
    @Mapping(target = "n", ignore = true)
    @Mapping(target = "p", ignore = true)
    @Mapping(target = "q", ignore = true)
    @Mapping(target = "qi", ignore = true)
    JsonWebKeyModel convertJsonWebKey(ReadOnlyEcKeyVaultKeyEntity source, URI vaultUri);

    @Mapping(target = "id", expression = "java(source.getId().asUri(vaultUri).toString())")
    @Mapping(target = "keyOps", source = "source.operations")
    @Mapping(target = "curveName", ignore = true)
    @Mapping(target = "k", ignore = true) //Do not return the private key.
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
    JsonWebKeyModel convertJsonWebKey(ReadOnlyAesKeyVaultKeyEntity source, URI vaultUri);

}
