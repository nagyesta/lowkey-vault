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
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.util.List;

import static com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil.generateAes;

@Slf4j
public class AesKeyVaultKeyEntity
        extends KeyVaultKeyEntity<SecretKey, Integer> implements ReadOnlyAesKeyVaultKeyEntity {

    public AesKeyVaultKeyEntity(
            final VersionedKeyEntityId id,
            final VaultFake vault,
            @Nullable final Integer keyParam,
            final boolean hsm) {
        super(id, vault, generateAes(keyParam), KeyType.OCT_HSM.validateOrDefault(keyParam, Integer.class), hsm);
    }

    public AesKeyVaultKeyEntity(
            final VersionedKeyEntityId id,
            final VaultFake vault,
            final SecretKey key,
            @Nullable final Integer keySize,
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
            final byte[] clear,
            final EncryptionAlgorithm encryptionAlgorithm,
            final byte @Nullable [] iv) {
        Assert.state(getOperations().contains(KeyOperation.ENCRYPT), getId() + " does not have ENCRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.WRAP_KEY), getId() + " does not have WRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        Assert.isTrue(this.getKeySize() == encryptionAlgorithm.getMaxKeySize(),
                "Key size (" + getKeySize() + ") is not matching the size required by the selected algorithm: "
                        + encryptionAlgorithm.getValue());
        Assert.isTrue(iv != null && iv.length > 0, "IV must not be null.");
        return doCrypto(() -> {
            final var cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.ENCRYPT_MODE, this.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(clear);
        }, "Cannot encrypt message.", log);
    }

    @Override
    public byte[] decryptToBytes(
            final byte[] encrypted,
            final EncryptionAlgorithm encryptionAlgorithm,
            final byte @Nullable [] iv) {
        Assert.state(getOperations().contains(KeyOperation.DECRYPT), getId() + " does not have DECRYPT operation assigned.");
        Assert.state(getOperations().contains(KeyOperation.UNWRAP_KEY), getId() + " does not have UNWRAP_KEY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        Assert.isTrue(this.getKeySize() == encryptionAlgorithm.getMaxKeySize(),
                "Key size (" + getKeySize() + ") is not matching the size required by the selected algorithm: "
                        + encryptionAlgorithm.getValue());
        Assert.isTrue(iv != null && iv.length > 0, "IV must not be null.");
        return doCrypto(() -> {
            final var cipher = Cipher.getInstance(encryptionAlgorithm.getAlg(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            cipher.init(Cipher.DECRYPT_MODE, this.getKey(), new IvParameterSpec(iv));
            return cipher.doFinal(encrypted);
        }, "Cannot decrypt message.", log);
    }

    @Override
    public byte[] signBytes(
            final byte[] digest,
            final SignatureAlgorithm encryptionAlgorithm) {
        throw new UnsupportedOperationException("Sign is not supported for OCT keys.");
    }

    @Override
    public boolean verifySignedBytes(
            final byte[] digest,
            final SignatureAlgorithm encryptionAlgorithm,
            final byte[] signature) {
        throw new UnsupportedOperationException("Verify is not supported for OCT keys.");
    }
}
