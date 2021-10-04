package com.github.nagyesta.lowkeyvault.service.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import org.springframework.util.Assert;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public interface ReadOnlyKeyVaultKeyEntity extends BaseVaultEntity {

    KeyType getKeyType();

    default byte[] encrypt(final String clear, final EncryptionAlgorithm encryptionAlgorithm,
                           final byte[] iv, final byte[] aad, final byte[] tag) {
        Assert.hasText(clear, "Clear text must not be blank.");
        return encryptBytes(clear.getBytes(StandardCharsets.UTF_8), encryptionAlgorithm, iv, aad, tag);
    }

    default String decrypt(final byte[] encrypted, final EncryptionAlgorithm encryptionAlgorithm,
                           final byte[] iv, final byte[] aad, final byte[] tag) {
        return new String(decryptToBytes(encrypted, encryptionAlgorithm, iv, aad, tag));
    }

    byte[] encryptBytes(byte[] clear, EncryptionAlgorithm encryptionAlgorithm,
                        byte[] iv, byte[] aad, byte[] tag);

    byte[] decryptToBytes(byte[] encrypted, EncryptionAlgorithm encryptionAlgorithm,
                          byte[] iv, byte[] aad, byte[] tag);

    VersionedKeyEntityId getId();

    URI getUri();

    List<KeyOperation> getOperations();

}
