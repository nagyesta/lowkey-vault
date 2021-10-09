package com.github.nagyesta.lowkeyvault.service.vault;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultStub;

import java.net.URI;

public interface VaultStub {

    boolean matches(URI vaultUri);

    URI baseUri();

    KeyVaultStub keyVaultStub();

    SecretVaultStub secretVaultStub();

    CertificateVaultStub certificateVaultStub();

    RecoveryLevel getRecoveryLevel();

    Integer getRecoverableDays();

}
