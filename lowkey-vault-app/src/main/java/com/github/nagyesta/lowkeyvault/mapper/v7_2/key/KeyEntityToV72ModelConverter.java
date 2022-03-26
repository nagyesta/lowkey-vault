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
    protected <M extends KeyVaultKeyModel> M mapActiveFields(final ReadOnlyKeyVaultKeyEntity source, final M model) {
        model.setKey(mapJsonWebKey(source));
        model.setAttributes(keyEntityToV72PropertiesModelConverter.convert(source));
        model.setTags(source.getTags());
        return model;
    }

    private JsonWebKeyModel mapJsonWebKey(final ReadOnlyKeyVaultKeyEntity source) {
        final JsonWebKeyModel jsonWebKeyModel;
        if (source.getKeyType().isRsa()) {
            jsonWebKeyModel = mapRsaFields((ReadOnlyRsaKeyVaultKeyEntity) source);
        } else if (source.getKeyType().isEc()) {
            jsonWebKeyModel = mapEcFields((ReadOnlyEcKeyVaultKeyEntity) source);
        } else {
            Assert.isTrue(source.getKeyType().isOct(), "Unknown key type found: " + source.getKeyType());
            jsonWebKeyModel = mapOctFields((ReadOnlyAesKeyVaultKeyEntity) source);
        }
        return jsonWebKeyModel;
    }

    private JsonWebKeyModel mapRsaFields(final ReadOnlyRsaKeyVaultKeyEntity entity) {
        final JsonWebKeyModel jsonWebKeyModel = mapCommonKeyProperties(entity);
        jsonWebKeyModel.setN(entity.getN());
        jsonWebKeyModel.setE(entity.getE());
        return jsonWebKeyModel;
    }

    private JsonWebKeyModel mapEcFields(final ReadOnlyEcKeyVaultKeyEntity entity) {
        final JsonWebKeyModel jsonWebKeyModel = mapCommonKeyProperties(entity);
        jsonWebKeyModel.setCurveName(entity.getKeyCurveName());
        jsonWebKeyModel.setX(entity.getX());
        jsonWebKeyModel.setY(entity.getY());
        return jsonWebKeyModel;
    }

    private JsonWebKeyModel mapOctFields(final ReadOnlyAesKeyVaultKeyEntity entity) {
        //Do not map K to avoid exposing key material
        return mapCommonKeyProperties(entity);
    }

    private JsonWebKeyModel mapCommonKeyProperties(final ReadOnlyKeyVaultKeyEntity entity) {
        final JsonWebKeyModel jsonWebKeyModel = new JsonWebKeyModel();
        jsonWebKeyModel.setId(entity.getUri().toString());
        jsonWebKeyModel.setKeyType(entity.getKeyType());
        jsonWebKeyModel.setKeyOps(entity.getOperations());
        return jsonWebKeyModel;
    }
}
