package com.github.nagyesta.lowkeyvault.service.common.impl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Getter
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class KeyVaultLifecycleAwareEntity {
    private OffsetDateTime created;
    private Optional<OffsetDateTime> notBefore;
    private Optional<OffsetDateTime> expiry;
    private OffsetDateTime updated;
    @Setter
    private boolean enabled;

    protected KeyVaultLifecycleAwareEntity() {
        this.enabled = true;
        this.created = now();
        this.updated = now();
        this.notBefore = Optional.empty();
        this.expiry = Optional.empty();
    }

    public void setNotBefore(final OffsetDateTime notBefore) {
        this.notBefore = Optional.ofNullable(notBefore);
    }

    public void setExpiry(final OffsetDateTime expiry) {
        this.expiry = Optional.ofNullable(expiry);
    }

    public void timeShift(final int offsetSeconds) {
        Assert.isTrue(offsetSeconds > 0, "Offset must be positive.");
        created = created.minusSeconds(offsetSeconds);
        updated = updated.minusSeconds(offsetSeconds);
        notBefore = notBefore.map(offsetDateTime -> offsetDateTime.minusSeconds(offsetSeconds));
        expiry = expiry.map(offsetDateTime -> offsetDateTime.minusSeconds(offsetSeconds));
    }

    public void setCreatedOn(@NonNull final OffsetDateTime createdOn) {
        this.created = createdOn;
    }

    public void setUpdatedOn(@NonNull final OffsetDateTime updatedOn) {
        this.updated = updatedOn;
    }

    protected void updatedNow() {
        this.updated = now();
    }

    protected OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
