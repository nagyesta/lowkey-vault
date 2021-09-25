package com.github.nagyesta.lowkeyvault.service.key;

public interface ReadOnlyRsaKeyVaultKeyEntity extends ReadOnlyKeyVaultKeyEntity {

    byte[] getN();

    byte[] getE();

    int getKeySize();
}
