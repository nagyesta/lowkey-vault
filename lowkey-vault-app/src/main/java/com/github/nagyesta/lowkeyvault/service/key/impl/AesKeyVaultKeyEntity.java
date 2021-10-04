package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Slf4j
public class AesKeyVaultKeyEntity extends KeyVaultKeyEntity<Map<Integer, SecretKey>, Integer> implements ReadOnlyAesKeyVaultKeyEntity {

    public AesKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @NonNull final VaultStub vault,
                                final Integer keyParam,
                                final boolean hsm) {
        super(id, vault, generate(keyParam), KeyType.OCT_HSM.validateOrDefault(keyParam, Integer.class), hsm);
    }

    private static Map<Integer, SecretKey> generate(final Integer keySize) {
        try {
            //Work around for: https://github.com/Azure/azure-sdk-for-java/issues/24507
            final SortedSet<Integer> sizes = Optional.ofNullable(keySize)
                    .map(Collections::singleton)
                    .map(TreeSet::new)
                    .orElse(new TreeSet<>(KeyType.OCT_HSM.getValidKeyParameters(Integer.class)));
            final Map<Integer, SecretKey> keys = new TreeMap<>();
            for (final Integer size : sizes) {
                final KeyGenerator keyGen = KeyGenerator.getInstance(KeyType.OCT_HSM.getAlgorithmName(), new BouncyCastleProvider());
                keyGen.init(KeyType.OCT_HSM.validateOrDefault(keySize, Integer.class));
                keys.put(size, keyGen.generateKey());
            }
            return keys;
        } catch (final NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate key.", e);
        }
    }

    @Override
    public KeyType getKeyType() {
        if (isHsm()) {
            return KeyType.OCT_HSM;
        } else {
            return KeyType.OCT;
        }
    }

    //Never return the key to the client
    @Override
    public byte[] getK() {
        return null;
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
        Assert.isTrue(this.getKey().containsKey(encryptionAlgorithm.getMaxKeySize()),
                "Key size (" + getKeySize() + ") is not matching the size required by the selected algorithm: "
                        + encryptionAlgorithm.getValue());
        Assert.isTrue(iv != null, "IV must not be null.");
        final SecretKey secretKey = this.getKey().get(encryptionAlgorithm.getMaxKeySize());
        try {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), new BouncyCastleProvider());
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(clear);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Cannot encrypt message.", e);
        }
    }

    @Override
    public byte[] decryptToBytes(@NonNull final byte[] encrypted, @NonNull final EncryptionAlgorithm encryptionAlgorithm,
                                 final byte[] iv, final byte[] aad, final byte[] tag) {
        Assert.state(getOperations().contains(KeyOperation.DECRYPT), getId() + " does not have DECRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.UNWRAP_KEY), getId() + " does not have UNWRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        Assert.isTrue(this.getKey().containsKey(encryptionAlgorithm.getMaxKeySize()),
                "Key size (" + getKeySize() + ") is not matching the size required by the selected algorithm: "
                        + encryptionAlgorithm.getValue());
        Assert.isTrue(iv != null, "IV must not be null.");
        final SecretKey secretKey = this.getKey().get(encryptionAlgorithm.getMaxKeySize());
        try {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), new BouncyCastleProvider());
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Cannot decrypt message.", e);
        }
    }
}
