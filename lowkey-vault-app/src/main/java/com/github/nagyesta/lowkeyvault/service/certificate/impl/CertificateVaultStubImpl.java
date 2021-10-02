package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultStub;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;

public class CertificateVaultStubImpl implements CertificateVaultStub {

    private final VaultStub vaultStub;

    public CertificateVaultStubImpl(@NonNull final VaultStub vaultStub) {
        this.vaultStub = vaultStub;
    }
}
