package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyAesKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.util.List;

import static com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil.generateAes;

@Slf4j
public class AesKeyVaultKeyEntity extends KeyVaultKeyEntity<SecretKey, Integer> implements ReadOnlyAesKeyVaultKeyEntity {

    public AesKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @NonNull final VaultFake vault,
                                final Integer keyParam,
                                final boolean hsm) {
        super(id, vault, generateAes(keyParam), KeyType.OCT_HSM.validateOrDefault(keyParam, Integer.class), hsm);
    }

    public AesKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @NonNull final VaultFake vault,
                                @NonNull final SecretKey key,
                                final Integer keySize,
                                final boolean hsm) {
        super(id, vault, key, KeyType.OCT_HSM.validateOrDefault(keySize, Integer.class), hsm);
    }

    @Override
    public KeyType getKeyType() {
        return KeyType.OCT_HSM;
    }

    @Override
    public KeyCreationInput<?> keyCreationInput() {
        return new OctKeyCreationInput(getKeyType(), getKeySize());
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
    protected List<KeyOperation> disallowedOperations() {
        return List.of(KeyOperation.SIGN, KeyOperation.VERIFY);
    }

    @Override
    public byte[] encryptBytes(
            @NonNull final byte[] clear, @NonNull final EncryptionAlgorithm encryptionAlgorithm,
            final byte[] iv) {
        Assert.state(getOperations().contains(KeyOperation.ENCRYPT), getId() + " does not have ENCRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.WRAP_KEY), getId() + " does not have WRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        Assert.isTrue(this.getKeySize() == encryptionAlgorithm.getMaxKeySize(),
                "Key size (" + getKeySize() + ") is not matching the size required by the selected algorithm: "
                        + encryptionAlgorithm.getValue());
        Assert.isTrue(iv != null, "IV must not be null.");
        return doCrypto(() -> {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, this.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(clear);
        }, "Cannot encrypt message.", log);
    }

    @Override
    public byte[] decryptToBytes(@NonNull final byte[] encrypted, @NonNull final EncryptionAlgorithm encryptionAlgorithm,
                                 final byte[] iv) {
        Assert.state(getOperations().contains(KeyOperation.DECRYPT), getId() + " does not have DECRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.UNWRAP_KEY), getId() + " does not have UNWRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        Assert.isTrue(this.getKeySize() == encryptionAlgorithm.getMaxKeySize(),
                "Key size (" + getKeySize() + ") is not matching the size required by the selected algorithm: "
                        + encryptionAlgorithm.getValue());
        Assert.isTrue(iv != null, "IV must not be null.");
        return doCrypto(() -> {
            final Cipher cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, this.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);
        }, "Cannot decrypt message.", log);
    }

    @Override
    public byte[] signBytes(final byte[] digest, final SignatureAlgorithm encryptionAlgorithm) {
        throw new UnsupportedOperationException("Sign is not supported for OCT keys.");
    }

    @Override
    public boolean verifySignedBytes(final byte[] digest,
                                     final SignatureAlgorithm encryptionAlgorithm,
                                     final byte[] signature) {
        throw new UnsupportedOperationException("Verify is not supported for OCT keys.");
    }
}
