package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class KeyVaultFakeImpl
        extends BaseVaultFakeImpl<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity, KeyVaultKeyEntity<?, ?>>
        implements KeyVaultFake {

    public KeyVaultFakeImpl(@org.springframework.lang.NonNull final VaultFake vaultFake,
                            @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                            final Integer recoverableDays) {
        super(vaultFake, recoveryLevel, recoverableDays);
    }

    @Override
    protected VersionedKeyEntityId createVersionedId(final String id, final String version) {
        return new VersionedKeyEntityId(vaultFake().baseUri(), id, version);
    }

    @Override
    public <E, T extends KeyCreationInput<E>> VersionedKeyEntityId createKeyVersion(
            @NonNull final String keyName, @NonNull final T input) {
        return input.getKeyType().createKey(this, keyName, input);
    }

    @Override
    public VersionedKeyEntityId createRsaKeyVersion(
            @NonNull final String keyName, @NonNull final RsaKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), keyName);
        final RsaKeyVaultKeyEntity keyEntity = new RsaKeyVaultKeyEntity(keyEntityId, vaultFake(),
                input.getKeyParameter(), input.getPublicExponent(), input.getKeyType().isHsm());
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId createEcKeyVersion(
            @NonNull final String keyName, @NonNull final EcKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), keyName);
        input.getKeyType().validate(input.getKeyParameter(), KeyCurveName.class);
        final EcKeyVaultKeyEntity keyEntity = new EcKeyVaultKeyEntity(keyEntityId, vaultFake(),
                input.getKeyParameter(), input.getKeyType().isHsm());
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public VersionedKeyEntityId createOctKeyVersion(
            @NonNull final String keyName, @NonNull final OctKeyCreationInput input) {
        final VersionedKeyEntityId keyEntityId = new VersionedKeyEntityId(vaultFake().baseUri(), keyName);
        Assert.isTrue(input.getKeyType().isHsm(), "OCT keys are only supported using HSM.");
        final AesKeyVaultKeyEntity keyEntity = new AesKeyVaultKeyEntity(keyEntityId, vaultFake(),
                input.getKeyParameter(), input.getKeyType().isHsm());
        return addVersion(keyEntityId, keyEntity);
    }

    @Override
    public void setKeyOperations(@NonNull final VersionedKeyEntityId keyEntityId,
                                 final List<KeyOperation> keyOperations) {
        getEntitiesInternal().getEntity(keyEntityId).setOperations(Objects.requireNonNullElse(keyOperations, Collections.emptyList()));
    }

}
