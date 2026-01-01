package com.github.nagyesta.lowkeyvault.service.vault.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.impl.SecretVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.util.Assert;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

@Slf4j
@EqualsAndHashCode(onlyExplicitlyIncluded = true, doNotUseGetters = true)
public class VaultFakeImpl implements VaultFake {

    @EqualsAndHashCode.Include
    private final URI vaultUri;
    @EqualsAndHashCode.Include
    private Set<URI> aliases;
    private final KeyVaultFake keys;
    private final SecretVaultFake secrets;
    private final CertificateVaultFake certificates;
    private final RecoveryLevel recoveryLevel;
    @Nullable
    private final Integer recoverableDays;
    private OffsetDateTime createdOn;
    @Nullable
    private OffsetDateTime deletedOn;

    public VaultFakeImpl(final URI vaultUri) {
        this(vaultUri, RecoveryLevel.RECOVERABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
    }

    public VaultFakeImpl(
            final URI vaultUri,
            final RecoveryLevel recoveryLevel,
            @Nullable final Integer recoverableDays) {
        recoveryLevel.checkValidRecoverableDays(recoverableDays);
        this.vaultUri = vaultUri;
        this.keys = new KeyVaultFakeImpl(this, recoveryLevel, recoverableDays);
        this.secrets = new SecretVaultFakeImpl(this, recoveryLevel, recoverableDays);
        this.certificates = new CertificateVaultFakeImpl(this, recoveryLevel, recoverableDays);
        this.recoveryLevel = recoveryLevel;
        this.recoverableDays = recoverableDays;
        this.createdOn = OffsetDateTime.now();
        this.aliases = Set.of();
    }

    @Override
    public boolean matches(
            final URI vaultUri,
            final UnaryOperator<URI> uriMapper) {
        final var lookupUri = uriMapper.apply(vaultUri);
        return uriMapper.apply(this.vaultUri).equals(lookupUri) || this.aliases.stream()
                .map(uriMapper)
                .anyMatch(lookupUri::equals);
    }

    @Override
    public URI baseUri() {
        return vaultUri;
    }

    @Override
    public Set<URI> aliases() {
        return aliases;
    }

    @Override
    public void setAliases(final Set<URI> aliases) {
        Assert.isTrue(!aliases.contains(baseUri()), "The base URI cannot be an alias as well.");
        this.aliases = Set.copyOf(aliases);
    }

    @Override
    public KeyVaultFake keyVaultFake() {
        return keys;
    }

    @Override
    public SecretVaultFake secretVaultFake() {
        return secrets;
    }

    @Override
    public CertificateVaultFake certificateVaultFake() {
        return certificates;
    }

    @Override
    public RecoveryLevel getRecoveryLevel() {
        return recoveryLevel;
    }

    @Override
    public @Nullable Integer getRecoverableDays() {
        return recoverableDays;
    }

    @Override
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public @Nullable OffsetDateTime getDeletedOn() {
        return deletedOn;
    }

    @Override
    public boolean isDeleted() {
        return !isActive();
    }

    @Override
    public boolean isActive() {
        return deletedOn == null;
    }

    @Override
    public boolean isExpired() {
        var result = false;
        if (isDeleted()) {
            final int recoverableDaysOffset = Objects.requireNonNullElse(recoverableDays, 0);
            //null-check done by isDeleted() call
            final var purgeDeadline = Objects.requireNonNull(deletedOn).plusDays(recoverableDaysOffset);
            result = purgeDeadline.isBefore(OffsetDateTime.now());
        }
        return result;
    }

    @Override
    public void delete() {
        Assert.state(!recoveryLevel.isSubscriptionProtected(),
                "Unable to delete subscription protected vault: " + baseUri());
        deletedOn = OffsetDateTime.now();
    }

    @Override
    public void recover() {
        Assert.state(isDeleted(), "Unable to recover a vault which is not deleted: " + baseUri());
        deletedOn = null;
    }

    @Override
    public void timeShift(
            final int offsetSeconds,
            final boolean regenerateCertificates) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        createdOn = createdOn.minusSeconds(offsetSeconds);
        deletedOn = Optional.ofNullable(deletedOn)
                .map(offsetDateTime -> offsetDateTime.minusSeconds(offsetSeconds))
                .orElse(null);
        keyVaultFake().timeShift(offsetSeconds);
        secretVaultFake().timeShift(offsetSeconds);
        certificateVaultFake().timeShift(offsetSeconds);
        if (regenerateCertificates) {
            log.info("Regenerating certificates of vault: {}", baseUri());
            certificateVaultFake().regenerateCertificates();
        }
    }
}
