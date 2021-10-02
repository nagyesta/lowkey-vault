package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import org.springframework.lang.NonNull;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Objects;

public class RsaKeyVaultKeyEntity extends KeyVaultKeyEntity<KeyPair, Integer> implements ReadOnlyRsaKeyVaultKeyEntity {

    public RsaKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @NonNull final VaultStub vault,
                                final Integer keyParam,
                                final BigInteger publicExponent,
                                final boolean hsm) {
        super(id, vault, generate(keyParam, publicExponent), Objects.requireNonNullElse(keyParam, KeyType.RSA.getDefaultKeySize()), hsm);
    }

    private static KeyPair generate(final Integer keySize, final BigInteger publicExponent) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeyType.RSA.getAlgorithmName());
            final int nonNullKeySize = Objects.requireNonNullElse(keySize, KeyType.RSA.getDefaultKeySize());
            keyGen.initialize(new RSAKeyGenParameterSpec(nonNullKeySize, publicExponent));
            return keyGen.generateKeyPair();
        } catch (final NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new CryptoException("Failed to generate key.", e);
        }
    }

    @Override
    public KeyType getKeyType() {
        if (isHsm()) {
            return KeyType.RSA_HSM;
        } else {
            return KeyType.RSA;
        }
    }

    @Override
    public byte[] getN() {
        return ((RSAPublicKey) getKey().getPublic()).getModulus().toByteArray();
    }

    @Override
    public byte[] getE() {
        return ((RSAPublicKey) getKey().getPublic()).getPublicExponent().toByteArray();
    }

    @Override
    public int getKeySize() {
        return getKeyParam();
    }

    @Override
    public byte[] encrypt(@NonNull final byte[] clear) {
        try {
            final Cipher cipher = Cipher.getInstance(getKey().getPublic().getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, getKey().getPublic());
            return cipher.doFinal(clear);
        } catch (final Exception e) {
            throw new CryptoException("Cannot encrypt message.", e);
        }
    }

    @Override
    public byte[] decrypt(@NonNull final byte[] encoded) {
        try {
            final Cipher cipher = Cipher.getInstance(getKey().getPrivate().getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, getKey().getPrivate());
            return cipher.doFinal(encoded);
        } catch (final Exception e) {
            throw new CryptoException("Cannot decrypt message.", e);
        }
    }
}
