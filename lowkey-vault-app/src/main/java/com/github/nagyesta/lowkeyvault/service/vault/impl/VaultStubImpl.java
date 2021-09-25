package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultStub;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateVaultStubImpl;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultStubImpl;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultStub;
import com.github.nagyesta.lowkeyvault.service.secret.impl.SecretVaultStubImpl;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.net.URI;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class VaultStubImpl implements VaultStub {

    @EqualsAndHashCode.Include
    private final URI vaultUri;
    private final KeyVaultStub keys;
    private final SecretVaultStub secrets;
    private final CertificateVaultStub certificates;
    private RecoveryLevel recoveryLevel;
    private Integer recoverableDays;

    public VaultStubImpl(@NonNull final URI vaultUri) {
        this.vaultUri = vaultUri;
        this.keys = new KeyVaultStubImpl(this);
        this.secrets = new SecretVaultStubImpl(this);
        this.certificates = new CertificateVaultStubImpl(this);
    }

    @Override
    public boolean matches(@NonNull final URI vaultUri) {
        return this.vaultUri.equals(vaultUri);
    }

    @Override
    public URI baseUri() {
        return vaultUri;
    }

    @Override
    public KeyVaultStub keyVaultStub() {
        return keys;
    }

    @Override
    public SecretVaultStub secretVaultStub() {
        return secrets;
    }

    @Override
    public CertificateVaultStub certificateVaultStub() {
        return certificates;
    }

    @Override
    public RecoveryLevel getRecoveryLevel() {
        return recoveryLevel;
    }

    @Override
    public Integer getRecoverableDays() {
        return recoverableDays;
    }

    @Override
    public void setDefaultRecovery(@NonNull final RecoveryLevel recoveryLevel, final Integer recoverableDays) {
        recoveryLevel.checkValidRecoverableDays(recoverableDays);
        this.recoveryLevel = recoveryLevel;
        this.recoverableDays = recoverableDays;
    }

}
