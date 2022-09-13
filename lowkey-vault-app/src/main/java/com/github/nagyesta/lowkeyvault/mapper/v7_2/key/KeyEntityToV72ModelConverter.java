package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.mapper.common.BaseRecoveryAwareConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.JsonWebKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.net.URI;

@Component
public class KeyEntityToV72ModelConverter
        extends BaseRecoveryAwareConverter<VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyVaultKeyModel, DeletedKeyVaultKeyModel> {

    private final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter;

    @Autowired
    public KeyEntityToV72ModelConverter(@NonNull final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter) {
        super(KeyVaultKeyModel::new, DeletedKeyVaultKeyModel::new);
        this.keyEntityToV72PropertiesModelConverter = keyEntityToV72PropertiesModelConverter;
    }

    @Override
    protected <M extends KeyVaultKeyModel> M mapActiveFields(final ReadOnlyKeyVaultKeyEntity source, final M model, final URI vaultUri) {
        model.setKey(mapJsonWebKey(source, vaultUri));
        model.setAttributes(keyEntityToV72PropertiesModelConverter.convert(source, vaultUri));
        model.setTags(source.getTags());
        model.setManaged(source.isManaged());
        return model;
    }

    private JsonWebKeyModel mapJsonWebKey(final ReadOnlyKeyVaultKeyEntity source, final URI vaultUri) {
        final JsonWebKeyModel jsonWebKeyModel;
        if (source.getKeyType().isRsa()) {
            jsonWebKeyModel = mapRsaFields((ReadOnlyRsaKeyVaultKeyEntity) source, vaultUri);
        } else if (source.getKeyType().isEc()) {
            jsonWebKeyModel = mapEcFields((ReadOnlyEcKeyVaultKeyEntity) source, vaultUri);
        } else {
            Assert.isTrue(source.getKeyType().isOct(), "Unknown key type found: " + source.getKeyType());
            jsonWebKeyModel = mapOctFields((ReadOnlyAesKeyVaultKeyEntity) source, vaultUri);
        }
        return jsonWebKeyModel;
    }

    private JsonWebKeyModel mapRsaFields(final ReadOnlyRsaKeyVaultKeyEntity entity, final URI vaultUri) {
        final JsonWebKeyModel jsonWebKeyModel = mapCommonKeyProperties(entity, vaultUri);
        jsonWebKeyModel.setN(entity.getN());
        jsonWebKeyModel.setE(entity.getE());
        return jsonWebKeyModel;
    }

    private JsonWebKeyModel mapEcFields(final ReadOnlyEcKeyVaultKeyEntity entity, final URI vaultUri) {
        final JsonWebKeyModel jsonWebKeyModel = mapCommonKeyProperties(entity, vaultUri);
        jsonWebKeyModel.setCurveName(entity.getKeyCurveName());
        jsonWebKeyModel.setX(entity.getX());
        jsonWebKeyModel.setY(entity.getY());
        return jsonWebKeyModel;
    }

    private JsonWebKeyModel mapOctFields(final ReadOnlyAesKeyVaultKeyEntity entity, final URI vaultUri) {
        //Do not map K to avoid exposing key material
        return mapCommonKeyProperties(entity, vaultUri);
    }

    private JsonWebKeyModel mapCommonKeyProperties(final ReadOnlyKeyVaultKeyEntity entity, final URI vaultUri) {
        final JsonWebKeyModel jsonWebKeyModel = new JsonWebKeyModel();
        jsonWebKeyModel.setId(entity.getId().asUri(vaultUri).toString());
        jsonWebKeyModel.setKeyType(entity.getKeyType());
        jsonWebKeyModel.setKeyOps(entity.getOperations());
        return jsonWebKeyModel;
    }
}
