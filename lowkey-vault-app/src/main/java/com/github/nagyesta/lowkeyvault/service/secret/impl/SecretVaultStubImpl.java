package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultStub;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;

public class SecretVaultStubImpl implements SecretVaultStub {

    private final VaultStub vaultStub;

    public SecretVaultStubImpl(@NonNull final VaultStub vaultStub,
                               @org.springframework.lang.NonNull final RecoveryLevel recoveryLevel,
                               final Integer recoverableDays) {
        this.vaultStub = vaultStub;
    }
}
