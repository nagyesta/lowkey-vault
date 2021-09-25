package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

public interface BaseVaultEntity {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    Optional<OffsetDateTime> getNotBefore();

    void setNotBefore(OffsetDateTime notBefore);

    Optional<OffsetDateTime> getExpiry();

    void setExpiry(OffsetDateTime expiry);

    OffsetDateTime getCreated();

    OffsetDateTime getUpdated();

    RecoveryLevel getRecoveryLevel();

    void setRecoveryLevel(RecoveryLevel recoveryLevel);

    Integer getRecoverableDays();

    void setRecoverableDays(Integer recoverableDays);

    Map<String, String> getTags();

    void setTags(Map<String, String> tags);
}
