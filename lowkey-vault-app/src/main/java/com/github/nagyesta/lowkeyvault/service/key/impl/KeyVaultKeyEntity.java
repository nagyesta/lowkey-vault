package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.NonNull;
import org.slf4j.Logger;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class KeyVaultKeyEntity<T, S> extends KeyVaultBaseEntity<VersionedKeyEntityId> implements ReadOnlyKeyVaultKeyEntity {

    private final T key;
    private final S keyParam;
    private final boolean hsm;
    private final VersionedKeyEntityId id;
    private final URI uri;
    private List<KeyOperation> operations;

    protected KeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @org.springframework.lang.NonNull final VaultFake vault,
                                @org.springframework.lang.NonNull final T key,
                                @org.springframework.lang.NonNull final S keyParam,
                                final boolean hsm) {
        super(vault);
        this.id = id;
        this.uri = id.asUri();
        this.key = key;
        this.keyParam = keyParam;
        this.hsm = hsm;
        this.operations = Collections.emptyList();
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
    public List<KeyOperation> getOperations() {
        return operations;
    }

    public void setOperations(final List<KeyOperation> operations) {
        this.updatedNow();
        this.operations = List.copyOf(operations);
    }

    protected <R> R doCrypto(final Callable<R> task, final String message, final Logger log) {
        try {
            return task.call();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException(message, e);
        }
    }

}
