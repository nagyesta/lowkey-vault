package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;

import java.net.URI;
import java.util.List;

public interface ReadOnlyKeyVaultKeyEntity extends BaseVaultEntity {

    KeyType getKeyType();

    byte[] encrypt(byte[] clear);

    byte[] decrypt(byte[] encoded);

    VersionedKeyEntityId getId();

    URI getUri();

    List<KeyOperation> getOperations();

}
