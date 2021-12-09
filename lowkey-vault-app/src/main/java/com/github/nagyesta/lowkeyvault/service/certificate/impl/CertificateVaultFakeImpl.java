package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;

public class CertificateVaultFakeImpl implements CertificateVaultFake {

    private final VaultFake vaultFake;

    public CertificateVaultFakeImpl(@NonNull final VaultFake vaultFake,
                                    @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                                    final Integer recoverableDays) {
        this.vaultFake = vaultFake;
    }
}
