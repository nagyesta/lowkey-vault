package com.github.nagyesta.lowkeyvault;

import com.azure.security.keyvault.keys.models.KeyCurveName;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jspecify.annotations.Nullable;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Objects;

public final class KeyGenUtil {

    public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

    private KeyGenUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    public static SecretKey generateAes(@Nullable final Integer keySize) {
        final int size = Objects.requireNonNullElse(keySize, 256);
        return keyGenerator("AES", size).generateKey();
    }

    public static KeyPair generateEc(final KeyCurveName keyCurveName) {
        return keyPairGenerator("EC", getAlgSpec(keyCurveName)).generateKeyPair();
    }

    public static KeyPair generateRsa(
            @Nullable final Integer keySize,
            @Nullable final BigInteger publicExponent) {
        final int nonNullKeySize = Objects.requireNonNullElse(keySize, 2048);
        final var notNullPublicExponent = Objects.requireNonNullElse(publicExponent, BigInteger.valueOf(65537));
        final var rsaKeyGenParameterSpec = new RSAKeyGenParameterSpec(nonNullKeySize, notNullPublicExponent);
        return keyPairGenerator("RSA", rsaKeyGenParameterSpec).generateKeyPair();
    }

    static KeyPairGenerator keyPairGenerator(
            final String algorithmName,
            final AlgorithmParameterSpec algSpec) {
        try {
            final var keyGen = KeyPairGenerator.getInstance(algorithmName, BOUNCY_CASTLE_PROVIDER);
            keyGen.initialize(algSpec);
            return keyGen;
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to generate key.", e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    static KeyGenerator keyGenerator(
            final String algorithmName,
            final int keySize) {
        try {
            final var keyGenerator = KeyGenerator.getInstance(algorithmName);
            keyGenerator.init(keySize);
            return keyGenerator;
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to generate key.", e);
        }
    }

    private static ECGenParameterSpec getAlgSpec(final KeyCurveName curve) {
        if (curve == KeyCurveName.P_256) {
            return new ECGenParameterSpec("secp256r1");
        } else if (curve == KeyCurveName.P_256K) {
            return new ECGenParameterSpec("secp256k1");
        } else if (curve == KeyCurveName.P_384) {
            return new ECGenParameterSpec("secp384r1");
        } else {
            return new ECGenParameterSpec("secp521r1");
        }
    }
}
