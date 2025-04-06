package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.*;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.Asn1ConverterUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.io.IOException;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.List;

import static com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil.generateEc;

@Slf4j
public class EcKeyVaultKeyEntity
        extends KeyVaultKeyEntity<KeyPair, KeyCurveName> implements ReadOnlyEcKeyVaultKeyEntity {

    public EcKeyVaultKeyEntity(
            @NonNull final VersionedKeyEntityId id,
            @NonNull final VaultFake vault,
            @NonNull final KeyCurveName keyParam,
            final boolean hsm) {
        super(id, vault, generateEc(keyParam), keyParam, hsm);
    }

    public EcKeyVaultKeyEntity(
            @NonNull final VersionedKeyEntityId id,
            @NonNull final VaultFake vault,
            @NonNull final KeyPair keyPair,
            final KeyCurveName curveName,
            final Boolean hsm) {
        super(id, vault, keyPair, KeyType.EC.validateOrDefault(curveName, KeyCurveName.class), hsm);
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
    public KeyCreationInput<?> keyCreationInput() {
        return new EcKeyCreationInput(getKeyType(), getKeyCurveName());
    }

    @Override
    public byte[] getX() {
        return normalizeKeyParameter(((ECPublicKey) getKey().getPublic()).getW().getAffineX().toByteArray());
    }

    @Override
    public byte[] getY() {
        return normalizeKeyParameter(((ECPublicKey) getKey().getPublic()).getW().getAffineY().toByteArray());
    }

    @Override
    public byte[] getD() {
        return ((ECPrivateKey) getKey().getPrivate()).getS().toByteArray();
    }

    @Override
    public KeyCurveName getKeyCurveName() {
        return getKeyParam();
    }

    @Override
    protected List<KeyOperation> disallowedOperations() {
        return List.of(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY, KeyOperation.ENCRYPT, KeyOperation.DECRYPT);
    }

    @Override
    public byte[] encryptBytes(
            final byte[] clear,
            final EncryptionAlgorithm encryptionAlgorithm, final byte[] iv) {
        throw new UnsupportedOperationException("Encrypt is not supported for EC keys.");
    }

    @Override
    public byte[] decryptToBytes(
            final byte[] encrypted,
            final EncryptionAlgorithm encryptionAlgorithm,
            final byte[] iv) {
        throw new UnsupportedOperationException("Decrypt is not supported for EC keys.");
    }

    @Override
    public byte[] signBytes(
            final byte[] digest,
            final SignatureAlgorithm signatureAlgorithm) {
        validateGenericSignOrVerifyInputs(digest, signatureAlgorithm, KeyOperation.SIGN);
        Assert.state(signatureAlgorithm.isCompatibleWithCurve(getKeyCurveName()), getId() + " is not using the right key curve.");
        final var signCallable = signCallable(digest, signatureAlgorithm, getKey().getPrivate());
        return doCrypto(signCallable, "Cannot sign message.", log);
    }

    @Override
    public boolean verifySignedBytes(
            final byte[] digest,
            final SignatureAlgorithm signatureAlgorithm,
            final byte[] signature) {
        validateGenericSignOrVerifyInputs(digest, signatureAlgorithm, KeyOperation.VERIFY);
        Assert.state(signatureAlgorithm.isCompatibleWithCurve(getKeyCurveName()), getId() + " is not using the right key curve.");
        final var verifyCallable = verifyCallable(digest, signatureAlgorithm, signature, getKey().getPublic());
        return doCrypto(verifyCallable, "Cannot verify digest message.", log);
    }

    @Override
    protected byte[] postProcessGeneratedSignature(final byte[] signature) {
        return Asn1ConverterUtil.convertFromAsn1toRaw(signature, getKeyCurveName().getByteLength());
    }

    @Override
    protected byte[] preProcessVerifiableSignature(final byte[] rawSignature) throws IOException {
        return Asn1ConverterUtil.convertFromRawToAsn1(rawSignature);
    }

    private byte[] normalizeKeyParameter(final byte[] byteArray) {
        final var expectedLength = getKeyParam().getByteLength();
        final var actualLength = byteArray.length;
        //if the actual length is larger, then there is a leading 0 byte in front of the actual value
        //this is added only because the next byte would be negative and the BigInteger would be negative as well
        if (actualLength > expectedLength) {
            return Arrays.copyOfRange(byteArray, actualLength - expectedLength, actualLength);
        } else {
            return byteArray;
        }
    }
}
