package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import lombok.NonNull;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;

import javax.crypto.KeyGenerator;
import java.net.URI;
import java.security.KeyPairGenerator;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class KeyVaultKeyEntity<T, S> extends KeyVaultBaseEntity implements ReadOnlyKeyVaultKeyEntity {

    private final T key;
    private final S keyParam;
    private final boolean hsm;
    private final VersionedKeyEntityId id;
    private final URI uri;
    private List<KeyOperation> operations;

    protected KeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @org.springframework.lang.NonNull final VaultStub vault,
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

    @SuppressWarnings("SameParameterValue")
    protected static KeyGenerator keyGenerator(final String algorithmName, final int keySize, final Logger log) {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithmName);
            keyGenerator.init(keySize);
            return keyGenerator;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate key.", e);
        }
    }

    protected static KeyPairGenerator keyPairGenerator(final String algorithmName,
                                                       final AlgorithmParameterSpec algSpec,
                                                       final Logger log) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithmName, new BouncyCastleProvider());
            keyGen.initialize(algSpec);
            return keyGen;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate key.", e);
        }
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
