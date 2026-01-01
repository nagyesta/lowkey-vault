package com.github.nagyesta.lowkeyvault.service.vault;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.function.UnaryOperator;

public interface VaultFake {

    boolean matches(URI vaultUri, UnaryOperator<URI> uriMapper);

    URI baseUri();

    Set<URI> aliases();

    void setAliases(Set<URI> aliases);

    KeyVaultFake keyVaultFake();

    SecretVaultFake secretVaultFake();

    CertificateVaultFake certificateVaultFake();

    RecoveryLevel getRecoveryLevel();

    @Nullable Integer getRecoverableDays();

    @Nullable OffsetDateTime getCreatedOn();

    @Nullable OffsetDateTime getDeletedOn();

    boolean isDeleted();

    boolean isActive();

    boolean isExpired();

    void delete();

    void recover();

    void timeShift(int offsetSeconds, boolean regenerateCertificates);
}
