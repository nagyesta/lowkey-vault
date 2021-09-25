package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.service.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.OctKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyCreationInput;

import java.util.List;

public interface KeyVaultStub extends BaseVaultStub<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> {

    <E, T extends KeyCreationInput<E>> VersionedKeyEntityId createKeyVersion(String keyName, T input);

    VersionedKeyEntityId createKeyVersion(String keyName, RsaKeyCreationInput input);

    VersionedKeyEntityId createKeyVersion(String keyName, EcKeyCreationInput input);

    VersionedKeyEntityId createKeyVersion(String keyName, OctKeyCreationInput input);

    void setKeyOperations(VersionedKeyEntityId keyEntityId, List<KeyOperation> keyOperations);

    <E extends ReadOnlyKeyVaultKeyEntity> E getEntity(VersionedKeyEntityId keyEntityId, Class<E> type);
}
