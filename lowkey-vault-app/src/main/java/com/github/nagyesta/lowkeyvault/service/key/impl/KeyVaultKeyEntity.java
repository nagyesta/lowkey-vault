package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import com.github.nagyesta.lowkeyvault.service.common.impl.KeyVaultBaseEntity;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.Getter;
import lombok.NonNull;
import org.slf4j.Logger;
import org.springframework.util.Assert;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Common Key entity base class.
 *
 * @param <T> The type of the key.
 * @param <S> The type of the key parameter.
 */
public abstract class KeyVaultKeyEntity<T, S> extends KeyVaultBaseEntity<VersionedKeyEntityId> implements ReadOnlyKeyVaultKeyEntity {

    @Getter
    private final T key;
    private final S keyParam;
    private final boolean hsm;
    private final VersionedKeyEntityId id;
    private List<KeyOperation> operations;

    protected KeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                                @org.springframework.lang.NonNull final VaultFake vault,
                                @org.springframework.lang.NonNull final T key,
                                @org.springframework.lang.NonNull final S keyParam,
                                final boolean hsm) {
        super(vault);
        this.id = id;
        this.key = key;
        this.keyParam = keyParam;
        this.hsm = hsm;
        this.operations = Collections.emptyList();
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
    public List<KeyOperation> getOperations() {
        return operations;
    }

    public void setOperations(final List<KeyOperation> operations) {
        final List<KeyOperation> invalid = operations.stream().filter(this.disallowedOperations()::contains).toList();
        Assert.isTrue(invalid.isEmpty(), "Operation not allowed for this key type: " + invalid + ".");
        this.updatedNow();
        this.operations = List.copyOf(operations);
    }

    protected List<KeyOperation> disallowedOperations() {
        return Collections.emptyList();
    }

    protected <R> R doCrypto(final Callable<R> task, final String message, final Logger log) {
        try {
            return task.call();
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException(message, e);
        }
    }

    protected void validateGenericSignOrVerifyInputs(
            final byte[] digest, final SignatureAlgorithm signatureAlgorithm, final KeyOperation keyOperation) {
        Assert.state(getOperations().contains(keyOperation),
                getId() + " does not have " + keyOperation.name() + " operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        signatureAlgorithm.getHashAlgorithm().verifyDigestLength(digest);
    }

    protected Callable<byte[]> signCallable(
            final byte[] digest, final SignatureAlgorithm signatureAlgorithm, final PrivateKey privateKey) {
        return () -> {
            final Signature sign = signatureAlgorithm.getSignatureInstance();
            sign.initSign(privateKey);
            sign.update(signatureAlgorithm.transformDigest(digest));
            final byte[] signature = sign.sign();
            return postProcessGeneratedSignature(signature);
        };
    }

    protected Callable<Boolean> verifyCallable(
            final byte[] digest, final SignatureAlgorithm signatureAlgorithm, final byte[] rawSignature, final PublicKey publicKey) {
        return () -> {
            final Signature verify = signatureAlgorithm.getSignatureInstance();
            verify.initVerify(publicKey);
            final byte[] signature = preProcessVerifiableSignature(rawSignature);
            verify.update(signatureAlgorithm.transformDigest(digest));
            return verify.verify(signature);
        };
    }

    protected byte[] postProcessGeneratedSignature(final byte[] signature) throws Exception {
        return signature;
    }

    protected byte[] preProcessVerifiableSignature(final byte[] rawSignature) throws Exception {
        return rawSignature;
    }
}
