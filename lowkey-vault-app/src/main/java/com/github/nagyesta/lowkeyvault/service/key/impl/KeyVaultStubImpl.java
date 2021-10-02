package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultStubImpl;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KeyVaultStubImpl
        extends BaseVaultStubImpl<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyVaultKeyEntity<?, ?>>
        implements KeyVaultStub {

    public KeyVaultStubImpl(@org.springframework.lang.NonNull final VaultStub vaultStub) {
        super(vaultStub);
    }

    @Override
    protected VersionedKeyEntityId createVersionedId(final String id, final String version) {
        return new VersionedKeyEntityId(vaultStub().baseUri(), id, version);
    }

    @Override
    public <E, T extends KeyCreationInput<E>> VersionedKeyEntityId createKeyVersion(
            @NonNull final String keyName, @NonNull final T input) {
        final KeyType keyType = input.getKeyType();
        if (keyType.isRsa()) {
            Assert.isInstanceOf(RsaKeyCreationInput.class, input);
            return createKeyVersion(keyName, (RsaKeyCreationInput) input);
        } else if (keyType.isEc()) {
            Assert.isInstanceOf(EcKeyCreationInput.class, input);
            return createKeyVersion(keyName, (EcKeyCreationInput) input);
        } else {
            Assert.isTrue(keyType.isOct(), "Unknown key type found: " + input.getKeyType());
            Assert.isInstanceOf(OctKeyCreationInput.class, input);
            return createKeyVersion(keyName, (OctKeyCreationInput) input);
        }
    }

    @Override
    public VersionedKeyEntityId createKeyVersion(
            @NonNull final String keyName, @NonNull final RsaKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultStub().baseUri(), keyName);
        final RsaKeyVaultKeyEntity keyEntity = new RsaKeyVaultKeyEntity(keyEntityId, vaultStub(),
                input.getKeyParameter(), input.getPublicExponent(), input.getKeyType().isHsm());
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId createKeyVersion(
            @NonNull final String keyName, @NonNull final EcKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultStub().baseUri(), keyName);
        final EcKeyVaultKeyEntity keyEntity = new EcKeyVaultKeyEntity(keyEntityId, vaultStub(),
                input.getKeyParameter(), input.getKeyType().isHsm());
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId createKeyVersion(
            @NonNull final String keyName, @NonNull final OctKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultStub().baseUri(), keyName);
        final AesKeyVaultKeyEntity keyEntity = new AesKeyVaultKeyEntity(keyEntityId, vaultStub(),
                input.getKeyParameter(), input.getKeyType().isHsm());
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public void setKeyOperations(@NonNull final VersionedKeyEntityId keyEntityId,
                                 final List<KeyOperation> keyOperations) {
        assertHasEntity(keyEntityId);
        doGetEntity(keyEntityId).setOperations(Objects.requireNonNullElse(keyOperations, Collections.emptyList()));
    }

}
