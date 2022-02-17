package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import org.springframework.util.Assert;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public interface ReadOnlyKeyVaultKeyEntity extends BaseVaultEntity<VersionedKeyEntityId>, ReadOnlyDeletedEntity<VersionedKeyEntityId> {

    KeyType getKeyType();

    default byte[] encrypt(final String clear, final EncryptionAlgorithm encryptionAlgorithm, final byte[] iv) {
        Assert.hasText(clear, "Clear text must not be blank.");
        return encryptBytes(clear.getBytes(StandardCharsets.UTF_8), encryptionAlgorithm, iv);
    }

    default String decrypt(final byte[] encrypted, final EncryptionAlgorithm encryptionAlgorithm, final byte[] iv) {
        return new String(decryptToBytes(encrypted, encryptionAlgorithm, iv));
    }

    byte[] encryptBytes(byte[] clear, EncryptionAlgorithm encryptionAlgorithm, byte[] iv);

    byte[] decryptToBytes(byte[] encrypted, EncryptionAlgorithm encryptionAlgorithm, byte[] iv);

    byte[] signBytes(byte[] digest, SignatureAlgorithm encryptionAlgorithm);

    boolean verifySignedBytes(byte[] digest, SignatureAlgorithm encryptionAlgorithm, byte[] signature);

    VersionedKeyEntityId getId();

    URI getUri();

    List<KeyOperation> getOperations();

}
