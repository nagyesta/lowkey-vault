package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public abstract class BaseEntityConverterRegistry<K extends EntityId, V extends K,
        E extends BaseVaultEntity<V>, M, DM extends M, PM extends BasePropertiesModel, IM, DIM extends IM,
        BLI extends BaseBackupListItem<PM>>
        implements EntityConverterRegistry<K, V, E, M, DM, PM, IM, DIM, BLI> {

    private final Map<String, RecoveryAwareConverter<E, M, DM>> modelConverters = new HashMap<>();
    private final Map<String, AliasAwareConverter<E, PM>> propertiesConverters = new HashMap<>();
    private final Map<String, RecoveryAwareConverter<E, IM, DIM>> itemConverters = new HashMap<>();
    private final Map<String, RecoveryAwareConverter<E, IM, DIM>> versionedItemConverters = new HashMap<>();
    private final Map<String, BackupConverter<K, V, E, PM, BLI>> backupConverters = new HashMap<>();

    @Override
    public RecoveryAwareConverter<E, M, DM> modelConverter(final String apiVersion) {
        return getNonNullConverterForApiVersion(modelConverters, apiVersion);
    }

    @Override
    public void registerModelConverter(final RecoveryAwareConverter<E, M, DM> converter) {
        converter.supportedVersions().forEach(v -> modelConverters.put(v, converter));
    }

    @Override
    public AliasAwareConverter<E, PM> propertiesConverter(final String apiVersion) {
        return getNonNullConverterForApiVersion(propertiesConverters, apiVersion);
    }

    @Override
    public void registerPropertiesConverter(final AliasAwareConverter<E, PM> converter) {
        converter.supportedVersions().forEach(v -> propertiesConverters.put(v, converter));
    }

    @Override
    public RecoveryAwareConverter<E, IM, DIM> itemConverter(final String apiVersion) {
        return getNonNullConverterForApiVersion(itemConverters, apiVersion);
    }

    @Override
    public void registerItemConverter(final RecoveryAwareConverter<E, IM, DIM> converter) {
        converter.supportedVersions().forEach(v -> itemConverters.put(v, converter));
    }

    @Override
    public RecoveryAwareConverter<E, IM, DIM> versionedItemConverter(final String apiVersion) {
        return getNonNullConverterForApiVersion(versionedItemConverters, apiVersion);
    }

    @Override
    public void registerVersionedItemConverter(final RecoveryAwareConverter<E, IM, DIM> converter) {
        converter.supportedVersions().forEach(v -> versionedItemConverters.put(v, converter));
    }

    @Override
    public BackupConverter<K, V, E, PM, BLI> backupConverter(final String apiVersion) {
        return getNonNullConverterForApiVersion(backupConverters, apiVersion);
    }

    @Override
    public void registerBackupConverter(final BackupConverter<K, V, E, PM, BLI> converter) {
        converter.supportedVersions().forEach(v -> backupConverters.put(v, converter));
    }

    private <CV> CV getNonNullConverterForApiVersion(final Map<String, CV> map, final String apiVersion) {
        if (!map.containsKey(apiVersion)) {
            throw new IllegalStateException("Unable to return converter for API version: "
                    + apiVersion + " available versions were: " + map.keySet());
        }
        return map.get(apiVersion);
    }
}
