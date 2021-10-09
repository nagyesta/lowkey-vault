package com.github.nagyesta.lowkeyvault.model.common;

public interface DeletedModel {
    String getRecoveryId();

    java.time.OffsetDateTime getDeletedDate();

    java.time.OffsetDateTime getScheduledPurgeDate();

    void setRecoveryId(String recoveryId);

    void setDeletedDate(java.time.OffsetDateTime deletedDate);

    void setScheduledPurgeDate(java.time.OffsetDateTime scheduledPurgeDate);
}
