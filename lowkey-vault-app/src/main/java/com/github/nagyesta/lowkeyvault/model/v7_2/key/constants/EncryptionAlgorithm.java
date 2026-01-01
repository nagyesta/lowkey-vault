package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

@SuppressWarnings("checkstyle:JavadocVariable")
public enum EncryptionAlgorithm {

    A128CBC("A128CBC", Constants.AES_CBC_ZERO_BYTE_PADDING, KeyType.OCT_HSM, 128, 128),
    A128CBCPAD("A128CBCPAD", Constants.AES_CBC_PKCS_5_PADDING, KeyType.OCT_HSM, 128, 128),
    A192CBC("A192CBC", Constants.AES_CBC_ZERO_BYTE_PADDING, KeyType.OCT_HSM, 192, 192),
    A192CBCPAD("A192CBCPAD", Constants.AES_CBC_PKCS_5_PADDING, KeyType.OCT_HSM, 192, 192),
    A256CBC("A256CBC", Constants.AES_CBC_ZERO_BYTE_PADDING, KeyType.OCT_HSM, 256, 256),
    A256CBCPAD("A256CBCPAD", Constants.AES_CBC_PKCS_5_PADDING, KeyType.OCT_HSM, 256, 256),
    RSA_OAEP("RSA-OAEP", Constants.RSA_NONE_OAEP_WITH_SHA_1_AND_MGF_1_PADDING, KeyType.RSA, 2048, 4096),
    RSA_OAEP_256("RSA-OAEP-256", Constants.RSA_NONE_OAEP_WITH_SHA_256_AND_MGF_1_PADDING, KeyType.RSA, 2048, 4096),
    RSA1_5("RSA1_5", Constants.RSA_NONE_PKCS_1_PADDING, KeyType.RSA, 2048, 4096);

    private final String value;
    private final String alg;
    private final KeyType compatibleType;
    private final int minKeySize;
    private final int maxKeySize;

    EncryptionAlgorithm(
            final String value,
            final String alg,
            final KeyType compatibleType,
            final int minKeySize,
            final int maxKeySize) {
        this.value = value;
        this.alg = alg;
        this.compatibleType = compatibleType;
        this.minKeySize = minKeySize;
        this.maxKeySize = maxKeySize;
    }

    @JsonCreator
    public static @Nullable EncryptionAlgorithm forValue(@Nullable final String name) {
        return Arrays.stream(values())
                .filter(algorithm -> algorithm.getValue().equals(name))
                .findFirst()
                .orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonIgnore
    public String getAlg() {
        return alg;
    }

    @JsonIgnore
    public boolean isCompatible(final KeyType type) {
        return compatibleType == type;
    }

    @JsonIgnore
    public int getMinKeySize() {
        return minKeySize;
    }

    @JsonIgnore
    public int getMaxKeySize() {
        return maxKeySize;
    }

    private static final class Constants {
        public static final String AES_CBC_ZERO_BYTE_PADDING = "AES/CBC/ZeroBytePadding";
        public static final String AES_CBC_PKCS_5_PADDING = "AES/CBC/PKCS5Padding";
        public static final String RSA_NONE_OAEP_WITH_SHA_1_AND_MGF_1_PADDING = "RSA/None/OAEPWithSHA1AndMGF1Padding";
        public static final String RSA_NONE_OAEP_WITH_SHA_256_AND_MGF_1_PADDING = "RSA/None/OAEPWithSHA256AndMGF1Padding";
        public static final String RSA_NONE_PKCS_1_PADDING = "RSA/None/PKCS1Padding";
    }
}
