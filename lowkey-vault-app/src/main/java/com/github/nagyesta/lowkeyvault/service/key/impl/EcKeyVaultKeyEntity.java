package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.*;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.reflect.Array;
import java.security.KeyPair;
import java.security.Signature;
import java.util.Optional;

@Slf4j
public class EcKeyVaultKeyEntity extends KeyVaultKeyEntity<KeyPair, KeyCurveName> implements ReadOnlyEcKeyVaultKeyEntity {

    public EcKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                               @NonNull final VaultFake vault,
                               @NonNull final KeyCurveName keyParam,
                               final boolean hsm) {
        super(id, vault, generate(keyParam), keyParam, hsm);
    }

    private static KeyPair generate(@lombok.NonNull final KeyCurveName keyCurveName) {
        return keyPairGenerator(KeyType.EC.getAlgorithmName(), keyCurveName.getAlgSpec(), log).generateKeyPair();
    }

    @Override
    public KeyType getKeyType() {
        if (isHsm()) {
            return KeyType.EC_HSM;
        } else {
            return KeyType.EC;
        }
    }

    @Override
    public byte[] getX() {
        return ((BCECPublicKey) getKey().getPublic()).getQ().getAffineXCoord().getEncoded();
    }

    @Override
    public byte[] getY() {
        return ((BCECPublicKey) getKey().getPublic()).getQ().getAffineYCoord().getEncoded();
    }

    @Override
    public KeyCurveName getKeyCurveName() {
        return getKeyParam();
    }

    @Override
    public byte[] encryptBytes(final byte[] clear, final EncryptionAlgorithm encryptionAlgorithm, final byte[] iv) {
        throw new UnsupportedOperationException("Encrypt is not supported for EC keys.");
    }

    @Override
    public byte[] decryptToBytes(final byte[] encrypted, final EncryptionAlgorithm encryptionAlgorithm, final byte[] iv) {
        throw new UnsupportedOperationException("Decrypt is not supported for EC keys.");
    }

    @Override
    public byte[] signBytes(final byte[] digest, final SignatureAlgorithm signatureAlgorithm) {
        Assert.state(getOperations().contains(KeyOperation.SIGN), getId() + " does not have SIGN operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        Assert.state(signatureAlgorithm.isCompatibleWithCurve(getKeyCurveName()), getId() + " is not using the right key curve.");
        final int length = Optional.ofNullable(digest)
                .map(Array::getLength)
                .orElseThrow(() -> new IllegalArgumentException("Digest is null."));
        Assert.isTrue(signatureAlgorithm.supportsDigestLength(length), getId() + " does not support digest length: " + length + ".");
        return doCrypto(() -> {
            final Signature ecSign = Signature.getInstance(signatureAlgorithm.getAlg());
            ecSign.initSign(getKey().getPrivate());
            ecSign.update(digest);
            return ecSign.sign();
        }, "Cannot sign message.", log);
    }

    @Override
    public boolean verifySignedBytes(final byte[] digest,
                                     final SignatureAlgorithm signatureAlgorithm,
                                     final byte[] signature) {
        Assert.state(getOperations().contains(KeyOperation.VERIFY), getId() + " does not have VERIFY operation assigned.");
        Assert.state(isEnabled(), getId() + " is not enabled.");
        Assert.state(signatureAlgorithm.isCompatibleWithCurve(getKeyCurveName()), getId() + " is not using the right key curve.");
        final int length = Optional.ofNullable(digest)
                .map(Array::getLength)
                .orElseThrow(() -> new IllegalArgumentException("Digest is null."));
        Assert.isTrue(signatureAlgorithm.supportsDigestLength(length), getId() + " does not support digest length: " + length + ".");
        return doCrypto(() -> {
            final Signature ecVerify = Signature.getInstance(signatureAlgorithm.getAlg());
            ecVerify.initVerify(getKey().getPublic());
            ecVerify.update(digest);
            return ecVerify.verify(signature);
        }, "Cannot verify digest message.", log);
    }
}
