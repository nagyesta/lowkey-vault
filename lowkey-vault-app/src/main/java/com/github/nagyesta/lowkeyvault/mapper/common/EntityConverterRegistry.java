package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;

import java.net.URI;

@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public interface EntityConverterRegistry<K extends EntityId, V extends K,
        E extends BaseVaultEntity<V>, M, DM extends M, PM extends BasePropertiesModel, IM, DIM extends IM,
        BLI extends BaseBackupListItem<PM>> {

    K entityId(URI baseUri, String name);

    V versionedEntityId(URI baseUri, String name, String version);

    RecoveryAwareConverter<E, M, DM> modelConverter(String apiVersion);

    void registerModelConverter(RecoveryAwareConverter<E, M, DM> converter);

    AliasAwareConverter<E, PM> propertiesConverter(String apiVersion);

    void registerPropertiesConverter(AliasAwareConverter<E, PM> converter);

    RecoveryAwareConverter<E, IM, DIM> itemConverter(String apiVersion);

    void registerItemConverter(RecoveryAwareConverter<E, IM, DIM> converter);

    RecoveryAwareConverter<E, IM, DIM> versionedItemConverter(String apiVersion);

    void registerVersionedItemConverter(RecoveryAwareConverter<E, IM, DIM> converter);

    BackupConverter<K, V, E, PM, BLI> backupConverter(String apiVersion);

    void registerBackupConverter(BackupConverter<K, V, E, PM, BLI> converter);
}
