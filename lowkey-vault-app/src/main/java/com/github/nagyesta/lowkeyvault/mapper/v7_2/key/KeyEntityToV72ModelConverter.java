package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.JsonWebKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

@Component
public class KeyEntityToV72ModelConverter implements Converter<ReadOnlyKeyVaultKeyEntity, KeyVaultKeyModel> {

    private final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter;

    @Autowired
    public KeyEntityToV72ModelConverter(@NonNull final KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter) {
        this.keyEntityToV72PropertiesModelConverter = keyEntityToV72PropertiesModelConverter;
    }

    @Override
    @org.springframework.lang.NonNull
    public KeyVaultKeyModel convert(@org.springframework.lang.NonNull final ReadOnlyKeyVaultKeyEntity source) {
        final JsonWebKeyModel key = mapJsonWebKey(source);
        final KeyPropertiesModel attributes = keyEntityToV72PropertiesModelConverter.convert(source);
        final Map<String, String> tags = source.getTags();
        return new KeyVaultKeyModel(attributes, key, tags);
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
