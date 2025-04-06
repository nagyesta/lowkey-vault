package com.github.nagyesta.lowkeyvault.service.key;

public interface ReadOnlyRsaKeyVaultKeyEntity
        extends ReadOnlyAsymmetricKeyVaultKeyEntity {

    byte[] getN();

    byte[] getE();

    int getKeySize();

    byte[] getD();

    byte[] getDp();

    byte[] getDq();

    byte[] getP();

    byte[] getQ();

    byte[] getQi();
}
