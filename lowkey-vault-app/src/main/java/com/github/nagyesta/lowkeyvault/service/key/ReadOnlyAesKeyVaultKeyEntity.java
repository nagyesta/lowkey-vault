package com.github.nagyesta.lowkeyvault.service.key;

public interface ReadOnlyAesKeyVaultKeyEntity extends ReadOnlyKeyVaultKeyEntity {

    byte[] getK();

    int getKeySize();
}
