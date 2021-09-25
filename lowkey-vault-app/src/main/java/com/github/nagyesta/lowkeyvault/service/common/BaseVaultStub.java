package com.github.nagyesta.lowkeyvault.service.common;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.EntityId;

import java.time.OffsetDateTime;
import java.util.Deque;
import java.util.Map;

public interface BaseVaultStub<K extends EntityId, V extends K, E extends BaseVaultEntity> {

    Deque<String> getVersions(K entityId);

    V getLatestVersionOfEntity(K entityId);

    void clearTags(V entityId);

    void addTags(V entityId, Map<String, String> tags);

    void setEnabled(V entityId, boolean enabled);

    void setExpiry(V entityId, OffsetDateTime notBefore, OffsetDateTime expiry);

    void setRecovery(V entityId, RecoveryLevel recoveryLevel, Integer recoverableDays);

    E getEntity(V entityId);

    <R extends E> R getEntity(V entityId, Class<R> type);
}
