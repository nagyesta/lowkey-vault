package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyDeletedEntity;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Base interface of vault entities.
 *
 * @param <V> The type of the versioned ID identifying this entity.
 */
public interface BaseVaultEntity<V extends EntityId> extends ReadOnlyDeletedEntity<V>, TimeAware {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    Optional<@Nullable OffsetDateTime> getNotBefore();

    void setNotBefore(@Nullable OffsetDateTime notBefore);

    Optional<@Nullable OffsetDateTime> getExpiry();

    void setExpiry(@Nullable OffsetDateTime expiry);

    OffsetDateTime getCreated();

    OffsetDateTime getUpdated();

    RecoveryLevel getRecoveryLevel();

    @Nullable Integer getRecoverableDays();

    Map<String, String> getTags();

    void setTags(Map<String, String> tags);

    void setDeletedDate(@Nullable OffsetDateTime deletedDate);

    void setScheduledPurgeDate(@Nullable OffsetDateTime scheduledPurgeDate);

    boolean isPurgeExpired();

    boolean canPurge();

    boolean isManaged();

    void setCreatedOn(OffsetDateTime createdOn);

    void setUpdatedOn(OffsetDateTime updatedOn);

    void setManaged(boolean managed);
}
