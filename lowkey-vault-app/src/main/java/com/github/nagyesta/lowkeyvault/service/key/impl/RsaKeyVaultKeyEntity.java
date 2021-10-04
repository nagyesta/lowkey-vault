package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

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
        super(id, vault, generate(keyParam, publicExponent), KeyType.RSA.validateOrDefault(keyParam, Integer.class), hsm);
    }

    private static KeyPair generate(final Integer keySize, final BigInteger publicExponent) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeyType.RSA.getAlgorithmName(), new BouncyCastleProvider());
            final int nonNullKeySize = KeyType.RSA.validateOrDefault(keySize, Integer.class);
            final BigInteger notNullPublicExponent = Objects.requireNonNullElse(publicExponent, BigInteger.valueOf(65537));
            keyGen.initialize(new RSAKeyGenParameterSpec(nonNullKeySize, notNullPublicExponent));
            return keyGen.generateKeyPair();
        } catch (final InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
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
    public byte[] encryptBytes(
            @NonNull final byte[] clear, @NonNull final EncryptionAlgorithm encryptionAlgorithm,
            final byte[] iv, final byte[] aad, final byte[] tag) {
        Assert.state(getOperations().contains(KeyOperation.ENCRYPT), getId() + " does not have ENCRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.WRAP_KEY), getId() + " does not have WRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        try {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, getKey().getPublic());
            return cipher.doFinal(clear);
        } catch (final Exception e) {
            throw new CryptoException("Cannot encrypt message.", e);
        }
    }

    @Override
    public byte[] decryptToBytes(@NonNull final byte[] encrypted, @NonNull final EncryptionAlgorithm encryptionAlgorithm,
                                 final byte[] iv, final byte[] aad, final byte[] tag) {
        Assert.state(getOperations().contains(KeyOperation.DECRYPT), getId() + " does not have DECRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.UNWRAP_KEY), getId() + " does not have UNWRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        try {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, getKey().getPrivate());
            return cipher.doFinal(encrypted);
        } catch (final Exception e) {
            throw new CryptoException("Cannot decrypt message.", e);
        }
    }
}
