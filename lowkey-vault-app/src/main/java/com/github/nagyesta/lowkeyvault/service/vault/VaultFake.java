package com.github.nagyesta.lowkeyvault.service.vault;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;

import java.net.URI;

public interface VaultFake {

    boolean matches(URI vaultUri);

    URI baseUri();

    KeyVaultFake keyVaultFake();

    SecretVaultFake secretVaultFake();

    CertificateVaultFake certificateVaultFake();

    RecoveryLevel getRecoveryLevel();

    Integer getRecoverableDays();

}
