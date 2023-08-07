package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.Callable;

import static com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil.generateRsa;

@Slf4j
public class RsaKeyVaultKeyEntity extends KeyVaultKeyEntity<KeyPair, Integer> implements ReadOnlyRsaKeyVaultKeyEntity {

    public RsaKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @NonNull final VaultFake vault,
                                final Integer keyParam,
                                final BigInteger publicExponent,
                                final boolean hsm) {
        super(id, vault, generateRsa(keyParam, publicExponent), KeyType.RSA.validateOrDefault(keyParam, Integer.class), hsm);
    }

    public RsaKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @NonNull final VaultFake vault,
                                @NonNull final KeyPair keyPair,
                                final Integer keySize,
                                final Boolean hsm) {
        super(id, vault, keyPair, KeyType.RSA.validateOrDefault(keySize, Integer.class), hsm);
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
    public KeyCreationInput<?> keyCreationInput() {
        return new RsaKeyCreationInput(getKeyType(), getKeySize(), ((RSAPublicKey) getKey().getPublic()).getPublicExponent());
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
    public byte[] getD() {
        return ((RSAPrivateCrtKey) getKey().getPrivate()).getPrivateExponent().toByteArray();
    }

    @Override
    public byte[] getDp() {
        return ((RSAPrivateCrtKey) getKey().getPrivate()).getPrimeExponentP().toByteArray();
    }

    @Override
    public byte[] getDq() {
        return ((RSAPrivateCrtKey) getKey().getPrivate()).getPrimeExponentQ().toByteArray();
    }

    @Override
    public byte[] getP() {
        return ((RSAPrivateCrtKey) getKey().getPrivate()).getPrimeP().toByteArray();
    }

    @Override
    public byte[] getQ() {
        return ((RSAPrivateCrtKey) getKey().getPrivate()).getPrimeQ().toByteArray();
    }

    @Override
    public byte[] getQi() {
        return ((RSAPrivateCrtKey) getKey().getPrivate()).getCrtCoefficient().toByteArray();
    }

    @Override
    public int getKeySize() {
        return getKeyParam();
    }

    @Override
    public byte[] encryptBytes(@NonNull final byte[] clear, @NonNull final EncryptionAlgorithm encryptionAlgorithm, final byte[] iv) {
        Assert.state(getOperations().contains(KeyOperation.ENCRYPT), getId() + " does not have ENCRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.WRAP_KEY), getId() + " does not have WRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        return doCrypto(() -> {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, getKey().getPublic());
            return cipher.doFinal(clear);
        }, "Cannot encrypt message.", log);
    }

    @Override
    public byte[] decryptToBytes(@NonNull final byte[] encrypted, @NonNull final EncryptionAlgorithm encryptionAlgorithm, final byte[] iv) {
        Assert.state(getOperations().contains(KeyOperation.DECRYPT), getId() + " does not have DECRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.UNWRAP_KEY), getId() + " does not have UNWRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        return doCrypto(() -> {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, getKey().getPrivate());
            return cipher.doFinal(encrypted);
        }, "Cannot decrypt message.", log);
    }

    @Override
    public byte[] signBytes(final byte[] digest, final SignatureAlgorithm signatureAlgorithm) {
        validateGenericSignOrVerifyInputs(digest, signatureAlgorithm, KeyOperation.SIGN);
        final Callable<byte[]> signCallable = signCallable(digest, signatureAlgorithm, getKey().getPrivate());
        return doCrypto(signCallable, "Cannot sign message.", log);
    }

    @Override
    public boolean verifySignedBytes(final byte[] digest,
                                     final SignatureAlgorithm signatureAlgorithm,
                                     final byte[] signature) {
        validateGenericSignOrVerifyInputs(digest, signatureAlgorithm, KeyOperation.VERIFY);
        final Callable<Boolean> verifyCallable = verifyCallable(digest, signatureAlgorithm, signature, getKey().getPublic());
        return doCrypto(verifyCallable, "Cannot verify digest message.", log);
    }
}
