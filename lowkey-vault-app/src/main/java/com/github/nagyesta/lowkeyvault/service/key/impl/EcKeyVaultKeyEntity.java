package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.lang.NonNull;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPublicKey;

public class EcKeyVaultKeyEntity extends KeyVaultKeyEntity<KeyPair, KeyCurveName> implements ReadOnlyEcKeyVaultKeyEntity {

    public EcKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                               @NonNull final VaultStub vault,
                               @NonNull final KeyCurveName keyParam,
                               final boolean hsm) {
        super(id, vault, generate(keyParam), keyParam, hsm);
    }

    private static KeyPair generate(@lombok.NonNull final KeyCurveName keyCurveName) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeyType.EC.getAlgorithmName(), new BouncyCastleProvider());
            keyGen.initialize(keyCurveName.getAlgSpec());
            return keyGen.generateKeyPair();
        } catch (final InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to generate key.", e);
        }
    }

    @Override
    public KeyType getKeyType() {
        if (isHsm()) {
            return KeyType.EC_HSM;
        } else {
            return KeyType.EC;
        }
    }

    @Override
    public byte[] getX() {
        return ((ECPublicKey) getKey().getPublic()).getW().getAffineX().toByteArray();
    }

    @Override
    public byte[] getY() {
        return ((ECPublicKey) getKey().getPublic()).getW().getAffineY().toByteArray();
    }

    @Override
    public KeyCurveName getKeyCurveName() {
        return getKeyParam();
    }

    @Override
    public byte[] encryptBytes(
            final byte[] clear, final EncryptionAlgorithm encryptionAlgorithm,
            final byte[] iv, final byte[] aad, final byte[] tag) {
        throw new UnsupportedOperationException("Encrypt is not supported for EC keys.");
    }

    @Override
    public byte[] decryptToBytes(final byte[] encrypted, final EncryptionAlgorithm encryptionAlgorithm,
                                 final byte[] iv, final byte[] aad, final byte[] tag) {
        throw new UnsupportedOperationException("Decrypt is not supported for EC keys.");
    }

}
