package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Base interface of vault entities.
 *
 * @param <V> The type of the versioned Id identifying this entity.
 */
public interface BaseVaultEntity<V extends EntityId> {

    V getId();

    boolean isEnabled();

    void setEnabled(boolean enabled);

    Optional<OffsetDateTime> getNotBefore();

    void setNotBefore(OffsetDateTime notBefore);

    Optional<OffsetDateTime> getExpiry();

    void setExpiry(OffsetDateTime expiry);

    OffsetDateTime getCreated();

    OffsetDateTime getUpdated();

    RecoveryLevel getRecoveryLevel();

    Integer getRecoverableDays();

    Map<String, String> getTags();

    void setTags(Map<String, String> tags);

    Optional<OffsetDateTime> getDeletedDate();

    void setDeletedDate(OffsetDateTime deletedDate);

    Optional<OffsetDateTime> getScheduledPurgeDate();

    void setScheduledPurgeDate(OffsetDateTime scheduledPurgeDate);

    boolean isPurgeExpired();

    boolean canPurge();
}
