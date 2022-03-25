package com.github.nagyesta.lowkeyvault.service.common.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class KeyVaultBaseEntity<V extends EntityId> extends KeyVaultLifecycleAwareEntity implements BaseVaultEntity<V> {
    private final RecoveryLevel recoveryLevel;
    private final Integer recoverableDays;
    private Map<String, String> tags;
    private Optional<OffsetDateTime> deletedDate;
    private Optional<OffsetDateTime> scheduledPurgeDate;
    private boolean managed;

    protected KeyVaultBaseEntity(@NonNull final VaultFake vault) {
        super();
        this.recoveryLevel = vault.getRecoveryLevel();
        this.recoverableDays = vault.getRecoverableDays();
        this.tags = Collections.emptyMap();
        this.deletedDate = Optional.empty();
        this.scheduledPurgeDate = Optional.empty();
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
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public void setTags(final Map<String, String> tags) {
        updatedNow();
        this.tags = Map.copyOf(tags);
    }

    @Override
    public Optional<OffsetDateTime> getDeletedDate() {
        return deletedDate;
    }

    @Override
    public void setDeletedDate(final OffsetDateTime deletedDate) {
        this.deletedDate = Optional.ofNullable(deletedDate);
    }

    @Override
    public Optional<OffsetDateTime> getScheduledPurgeDate() {
        return scheduledPurgeDate;
    }

    @Override
    public void setScheduledPurgeDate(final OffsetDateTime scheduledPurgeDate) {
        this.scheduledPurgeDate = Optional.ofNullable(scheduledPurgeDate);
    }

    @Override
    public boolean isPurgeExpired() {
        return getScheduledPurgeDate()
                .filter(date -> date.isBefore(now()))
                .isPresent();
    }

    @Override
    public boolean canPurge() {
        return getScheduledPurgeDate().isPresent() && getRecoveryLevel().isPurgeable();
    }

    @Override
    public void timeShift(final int offsetSeconds) {
        super.timeShift(offsetSeconds);
        deletedDate = deletedDate.map(offsetDateTime -> offsetDateTime.minusSeconds(offsetSeconds));
        scheduledPurgeDate = scheduledPurgeDate.map(offsetDateTime -> offsetDateTime.minusSeconds(offsetSeconds));
    }

    @Override
    public boolean isManaged() {
        return managed;
    }

    public void setManaged(final boolean managed) {
        this.managed = managed;
    }
}
