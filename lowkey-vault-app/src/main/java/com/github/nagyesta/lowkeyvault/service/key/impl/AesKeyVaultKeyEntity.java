package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import org.springframework.lang.NonNull;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class AesKeyVaultKeyEntity extends KeyVaultKeyEntity<SecretKey, Integer> implements ReadOnlyAesKeyVaultKeyEntity {

    public AesKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @NonNull final VaultStub vault,
                                final Integer keyParam,
                                final boolean hsm) {
        super(id, vault, generate(keyParam), Objects.requireNonNullElse(keyParam, KeyType.OCT.getDefaultKeySize()), hsm);
    }

    private static SecretKey generate(final Integer keySize) {
        try {
            final KeyGenerator keyGen = KeyGenerator.getInstance(KeyType.OCT.getAlgorithmName());
            keyGen.init(Objects.requireNonNullElse(keySize, KeyType.OCT.getDefaultKeySize()));
            return keyGen.generateKey();
        } catch (final NoSuchAlgorithmException e) {
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

    @Override
    public byte[] getK() {
        return getKey().getEncoded();
    }

    @Override
    public int getKeySize() {
        return getKeyParam();
    }

    @Override
    public byte[] encrypt(@NonNull final byte[] clear) {
        try {
            final Cipher cipher = Cipher.getInstance(getKey().getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            return cipher.doFinal(clear);
        } catch (final Exception e) {
            throw new CryptoException("Cannot encrypt message.", e);
        }
    }

    @Override
    public byte[] decrypt(@NonNull final byte[] encoded) {
        try {
            final Cipher cipher = Cipher.getInstance(getKey().getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            return cipher.doFinal(encoded);
        } catch (final Exception e) {
            throw new CryptoException("Cannot decrypt message.", e);
        }
    }
}
