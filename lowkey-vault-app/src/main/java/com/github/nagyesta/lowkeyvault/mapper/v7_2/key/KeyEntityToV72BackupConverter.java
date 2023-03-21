package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.SortedSet;

public class KeyEntityToV72BackupConverter
        extends BackupConverter<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel, KeyBackupListItem> {

    private final KeyConverterRegistry registry;

    @Autowired
    public KeyEntityToV72BackupConverter(@lombok.NonNull final KeyConverterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterPropertiesSet() {
        registry.registerBackupConverter(this);
    }

    @Override
    protected KeyBackupListItem convertUniqueFields(@NonNull final ReadOnlyKeyVaultKeyEntity source) {
        final JsonWebKeyImportRequest keyMaterial = convertKeyMaterial(source);
        final KeyBackupListItem listItem = new KeyBackupListItem();
        listItem.setKeyMaterial(populateCommonKeyFields(source, keyMaterial));
        return listItem;
    }

    @Override
    protected AliasAwareConverter<ReadOnlyKeyVaultKeyEntity, KeyPropertiesModel> propertiesConverter() {
        return registry.propertiesConverter(supportedVersions().last());
    }

    private JsonWebKeyImportRequest convertKeyMaterial(final ReadOnlyKeyVaultKeyEntity source) {
        final JsonWebKeyImportRequest keyMaterial = new JsonWebKeyImportRequest();
        if (source.getKeyType().isRsa()) {
            convertRsaFields((ReadOnlyRsaKeyVaultKeyEntity) source, keyMaterial);
        } else if (source.getKeyType().isEc()) {
            convertEcFields((ReadOnlyEcKeyVaultKeyEntity) source, keyMaterial);
        } else {
            Assert.isTrue(source.getKeyType().isOct(), "Unknown key type found: " + source.getKeyType());
            convertOctFields((ReadOnlyAesKeyVaultKeyEntity) source, keyMaterial);
        }
        return keyMaterial;
    }

    private void convertOctFields(final ReadOnlyAesKeyVaultKeyEntity source, final JsonWebKeyImportRequest keyMaterial) {
        keyMaterial.setK(source.getK());
    }

    private void convertEcFields(final ReadOnlyEcKeyVaultKeyEntity source, final JsonWebKeyImportRequest keyMaterial) {
        keyMaterial.setCurveName(source.getKeyCurveName());
        keyMaterial.setX(source.getX());
        keyMaterial.setY(source.getY());
        keyMaterial.setD(source.getD());
    }

    private void convertRsaFields(final ReadOnlyRsaKeyVaultKeyEntity source, final JsonWebKeyImportRequest keyMaterial) {
        keyMaterial.setN(source.getN());
        keyMaterial.setE(source.getE());
        keyMaterial.setD(source.getD());
        keyMaterial.setDp(source.getDp());
        keyMaterial.setDq(source.getDq());
        keyMaterial.setP(source.getP());
        keyMaterial.setQ(source.getQ());
        keyMaterial.setQi(source.getQi());
    }

    private JsonWebKeyImportRequest populateCommonKeyFields(
            final ReadOnlyKeyVaultKeyEntity source, final JsonWebKeyImportRequest keyMaterial) {
        keyMaterial.setId(source.getId().asUri(source.getId().vault()).toString());
        keyMaterial.setKeyType(source.getKeyType());
        keyMaterial.setKeyOps(source.getOperations());
        keyMaterial.setKeyHsm(null);
        return keyMaterial;
    }

    @Override
    public SortedSet<String> supportedVersions() {
        return ApiVersionAware.V7_2_AND_V7_3;
    }
}
