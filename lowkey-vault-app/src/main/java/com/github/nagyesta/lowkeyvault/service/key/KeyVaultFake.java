package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultFake;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.*;

import java.util.List;

public interface KeyVaultFake
        extends BaseVaultFake<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> {

    VersionedKeyEntityId createKeyVersion(String keyName, KeyCreateDetailedInput input);

    VersionedKeyEntityId createRsaKeyVersion(String keyName, RsaKeyCreationInput input);

    VersionedKeyEntityId createEcKeyVersion(String keyName, EcKeyCreationInput input);

    VersionedKeyEntityId createOctKeyVersion(String keyName, OctKeyCreationInput input);

    void setKeyOperations(VersionedKeyEntityId keyEntityId, List<KeyOperation> keyOperations);

    VersionedKeyEntityId importKeyVersion(String keyName, KeyImportInput key) throws CryptoException;

    VersionedKeyEntityId importKeyVersion(VersionedKeyEntityId keyEntityId, KeyImportInput key) throws CryptoException;

    VersionedKeyEntityId importEcKeyVersion(VersionedKeyEntityId keyEntityId, JsonWebKeyImportRequest key) throws CryptoException;

    VersionedKeyEntityId importRsaKeyVersion(VersionedKeyEntityId keyEntityId, JsonWebKeyImportRequest key) throws CryptoException;

    VersionedKeyEntityId importOctKeyVersion(VersionedKeyEntityId keyEntityId, JsonWebKeyImportRequest key) throws CryptoException;

    ReadOnlyRotationPolicy rotationPolicy(KeyEntityId keyEntityId);

    void setRotationPolicy(RotationPolicy rotationPolicy);

    VersionedKeyEntityId rotateKey(KeyEntityId entityId);
}
