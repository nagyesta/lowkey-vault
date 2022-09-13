package com.github.nagyesta.lowkeyvault.service.key.util;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Objects;

@Slf4j
public final class KeyGenUtil {

    /**
     * The single instance of the Bouncy Castle provider we need.
     */
    public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();

    private KeyGenUtil() {
        throw new IllegalCallerException("Utility cannot be instantiated.");
    }

    @org.springframework.lang.NonNull
    public static SecretKey generateAes(@Nullable final Integer keySize) {
        final int size = KeyType.OCT_HSM.validateOrDefault(keySize, Integer.class);
        return keyGenerator(KeyType.OCT_HSM.getAlgorithmName(), size).generateKey();
    }

    @org.springframework.lang.NonNull
    public static KeyPair generateEc(@NonNull final KeyCurveName keyCurveName) {
        return keyPairGenerator(KeyType.EC.getAlgorithmName(), keyCurveName.getAlgSpec()).generateKeyPair();
    }

    @org.springframework.lang.NonNull
    public static KeyPair generateRsa(@Nullable final Integer keySize, @Nullable final BigInteger publicExponent) {
        final int nonNullKeySize = KeyType.RSA.validateOrDefault(keySize, Integer.class);
        final BigInteger notNullPublicExponent = Objects.requireNonNullElse(publicExponent, BigInteger.valueOf(65537));
        final RSAKeyGenParameterSpec rsaKeyGenParameterSpec = new RSAKeyGenParameterSpec(nonNullKeySize, notNullPublicExponent);
        return keyPairGenerator(KeyType.RSA.getAlgorithmName(), rsaKeyGenParameterSpec).generateKeyPair();
    }

    @org.springframework.lang.NonNull
    public static byte[] generateRandomBytes(final int count) {
        return generateRandomBytes(count, "NativePRNG");
    }

    @org.springframework.lang.NonNull
    static byte[] generateRandomBytes(final int count, @org.springframework.lang.NonNull final String algorithm) {
        Assert.isTrue(count > 0, "Number of bytes must be greater than 0.");
        try {
            final byte[] bytes = new byte[count];
            SecureRandom.getInstance(algorithm).nextBytes(bytes);
            return bytes;
        } catch (final NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate random bytes.", e);
        }
    }

    static KeyPairGenerator keyPairGenerator(final String algorithmName,
                                             final AlgorithmParameterSpec algSpec) {
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithmName, BOUNCY_CASTLE_PROVIDER);
            keyGen.initialize(algSpec);
            return keyGen;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate key.", e);
        }
    }

    @SuppressWarnings("SameParameterValue")
    static KeyGenerator keyGenerator(final String algorithmName, final int keySize) {
        try {
            final KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithmName);
            keyGenerator.init(keySize);
            return keyGenerator;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate key.", e);
        }
    }

}
