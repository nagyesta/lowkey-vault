package com.github.nagyesta.lowkeyvault.service.common.impl;

import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class KeyVaultLifecycleAwareEntity {
    private OffsetDateTime created;
    private Optional<OffsetDateTime> notBefore;
    private Optional<OffsetDateTime> expiry;
    private OffsetDateTime updated;
    private boolean enabled;

    protected KeyVaultLifecycleAwareEntity() {
        this.enabled = true;
        this.created = now();
        this.updated = now();
        this.notBefore = Optional.empty();
        this.expiry = Optional.empty();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public Optional<OffsetDateTime> getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(final OffsetDateTime notBefore) {
        this.notBefore = Optional.ofNullable(notBefore);
    }

    public Optional<OffsetDateTime> getExpiry() {
        return expiry;
    }

    public void setExpiry(final OffsetDateTime expiry) {
        this.expiry = Optional.ofNullable(expiry);
    }

    public OffsetDateTime getCreated() {
        return created;
    }

    public OffsetDateTime getUpdated() {
        return updated;
    }

    public void timeShift(final int offsetSeconds) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        created = created.minusSeconds(offsetSeconds);
        updated = updated.minusSeconds(offsetSeconds);
        notBefore = notBefore.map(offsetDateTime -> offsetDateTime.minusSeconds(offsetSeconds));
        expiry = expiry.map(offsetDateTime -> offsetDateTime.minusSeconds(offsetSeconds));
    }

    protected void updatedNow() {
        this.updated = now();
    }

    protected OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
