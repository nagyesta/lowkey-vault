package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.common.BaseVaultEntity;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public abstract class KeyVaultKeyEntity<T, S> implements ReadOnlyKeyVaultKeyEntity, BaseVaultEntity {

    private final T key;
    private final S keyParam;
    private final boolean hsm;
    private final VersionedKeyEntityId id;
    private final URI uri;
    private final OffsetDateTime created;
    private boolean enabled;
    private Optional<OffsetDateTime> notBefore;
    private Optional<OffsetDateTime> expiry;
    private OffsetDateTime updated;
    private RecoveryLevel recoveryLevel;
    private Integer recoverableDays;
    private List<KeyOperation> operations;
    private Map<String, String> tags;

    public KeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                             @NonNull final VaultStub vault,
                             @NonNull final T key,
                             @NonNull final S keyParam,
                             final boolean hsm) {
        this.id = id;
        this.uri = id.asUri();
        this.key = key;
        this.keyParam = keyParam;
        this.created = now();
        this.updated = now();
        this.recoveryLevel = vault.getRecoveryLevel();
        this.recoverableDays = vault.getRecoverableDays();
        this.hsm = hsm;
        this.tags = Collections.emptyMap();
        this.operations = Collections.emptyList();
        this.expiry = Optional.empty();
        this.notBefore = Optional.empty();
    }

    protected T getKey() {
        return key;
    }

    protected S getKeyParam() {
        return keyParam;
    }

    protected boolean isHsm() {
        return hsm;
    }

    @Override
    public VersionedKeyEntityId getId() {
        return id;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Optional<OffsetDateTime> getNotBefore() {
        return notBefore;
    }

    @Override
    public void setNotBefore(final OffsetDateTime notBefore) {
        this.notBefore = Optional.ofNullable(notBefore);
    }

    @Override
    public Optional<OffsetDateTime> getExpiry() {
        return expiry;
    }

    @Override
    public void setExpiry(final OffsetDateTime expiry) {
        this.expiry = Optional.ofNullable(expiry);
    }

    @Override
    public OffsetDateTime getCreated() {
        return created;
    }

    @Override
    public OffsetDateTime getUpdated() {
        return updated;
    }

    @Override
    public RecoveryLevel getRecoveryLevel() {
        return recoveryLevel;
    }

    @Override
    public void setRecoveryLevel(final RecoveryLevel recoveryLevel) {
        this.updated = now();
        this.recoveryLevel = recoveryLevel;
    }

    @Override
    public Integer getRecoverableDays() {
        return recoverableDays;
    }

    @Override
    public void setRecoverableDays(final Integer recoverableDays) {
        this.updated = now();
        this.recoverableDays = recoverableDays;
    }

    @Override
    public List<KeyOperation> getOperations() {
        return operations;
    }

    public void setOperations(final List<KeyOperation> operations) {
        this.updated = now();
        this.operations = List.copyOf(operations);
    }

    @Override
    public Map<String, String> getTags() {
        return tags;
    }

    @Override
    public void setTags(final Map<String, String> tags) {
        this.updated = now();
        this.tags = Map.copyOf(tags);
    }

    private OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}
