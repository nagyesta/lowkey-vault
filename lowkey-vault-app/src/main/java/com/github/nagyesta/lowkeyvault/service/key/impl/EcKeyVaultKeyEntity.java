package com.github.nagyesta.lowkeyvault.service.key.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyEcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import org.springframework.lang.NonNull;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.interfaces.ECPublicKey;

public class EcKeyVaultKeyEntity extends KeyVaultKeyEntity<KeyPair, KeyCurveName> implements ReadOnlyEcKeyVaultKeyEntity {

    private SecretKeySpec secretKey;

    public EcKeyVaultKeyEntity(@NonNull final VersionedKeyEntityId id,
                               @NonNull final VaultStub vault,
                               @NonNull final KeyCurveName keyParam,
                               final boolean hsm) {
        super(id, vault, generate(keyParam), keyParam, hsm);
    }

    private static KeyPair generate(@lombok.NonNull final KeyCurveName keyCurveName) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KeyType.EC.getAlgorithmName());
            keyGen.initialize(keyCurveName.getAlgSpec());
            return keyGen.generateKeyPair();
        } catch (final InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to generate key.", e);
        }
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
        return ((ECPublicKey) getKey().getPublic()).getW().getAffineX().toByteArray();
    }

    @Override
    public byte[] getY() {
        return ((ECPublicKey) getKey().getPublic()).getW().getAffineY().toByteArray();
    }

    @Override
    public KeyCurveName getKeyCurveName() {
        return getKeyParam();
    }

    @Override
    public byte[] encrypt(@NonNull final byte[] clear) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, getKeyViaECDH());
            return cipher.doFinal(clear);
        } catch (final Exception e) {
            throw new CryptoException("Cannot encrypt message.", e);
        }
    }

    @Override
    public byte[] decrypt(@NonNull final byte[] encoded) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, getKeyViaECDH());
            return cipher.doFinal(encoded);
        } catch (final Exception e) {
            throw new CryptoException("Cannot decrypt message.", e);
        }
    }

    private SecretKey getKeyViaECDH() throws Exception {
        if (secretKey == null) {
            final KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(getKey().getPrivate());
            keyAgreement.doPhase(getKey().getPublic(), true);
            final byte[] sharedSecret = keyAgreement.generateSecret();
            final byte[] key = MessageDigest.getInstance("SHA-256").digest(sharedSecret);
            secretKey = new SecretKeySpec(key, "AES");
        }
        return secretKey;
    }
}
