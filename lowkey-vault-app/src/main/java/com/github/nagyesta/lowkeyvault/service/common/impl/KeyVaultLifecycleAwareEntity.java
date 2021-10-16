package com.github.nagyesta.lowkeyvault.service.common.impl;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class KeyVaultLifecycleAwareEntity {
    private final OffsetDateTime created;
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

    protected void updatedNow() {
        this.updated = now();
    }

    protected OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
