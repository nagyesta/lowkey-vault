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
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Objects;
import java.util.Random;

@Slf4j
public final class KeyGenUtil {

    /**
     * The single instance of the Bouncy Castle provider we need.
     */
    @SuppressWarnings("java:S2386") //this object can be reused
    public static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    @SuppressWarnings("java:S2245") //this is not intended to be used in a real-life scenario for cryptography
    private static final Random RANDOM = new Random();

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
        final var notNullPublicExponent = Objects.requireNonNullElse(publicExponent, BigInteger.valueOf(65537));
        final var rsaKeyGenParameterSpec = new RSAKeyGenParameterSpec(nonNullKeySize, notNullPublicExponent);
        return keyPairGenerator(KeyType.RSA.getAlgorithmName(), rsaKeyGenParameterSpec).generateKeyPair();
    }

    @org.springframework.lang.NonNull
    public static byte[] generateRandomBytes(final int count) {
        Assert.isTrue(count > 0, "Number of bytes must be greater than 0.");
        try {
            final var bytes = new byte[count];
            RANDOM.nextBytes(bytes);
            return bytes;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate random bytes.", e);
        }
    }

    static KeyPairGenerator keyPairGenerator(final String algorithmName,
                                             final AlgorithmParameterSpec algSpec) {
        try {
            final var keyGen = KeyPairGenerator.getInstance(algorithmName, BOUNCY_CASTLE_PROVIDER);
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
            final var keyGenerator = KeyGenerator.getInstance(algorithmName);
            keyGenerator.init(keySize);
            return keyGenerator;
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
            throw new CryptoException("Failed to generate key.", e);
        }
    }

}
