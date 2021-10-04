package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

@SuppressWarnings("checkstyle:JavadocVariable")
public enum EncryptionAlgorithm {

    A128CBC("A128CBC", "AES/CBC/ZeroBytePadding", KeyType.OCT, 128, 128),
    A128CBCPAD("A128CBCPAD", "AES/CBC/PKCS5Padding", KeyType.OCT, 128, 128),
    A192CBC("A192CBC", "AES/CBC/ZeroBytePadding", KeyType.OCT, 192, 192),
    A192CBCPAD("A192CBCPAD", "AES/CBC/PKCS5Padding", KeyType.OCT, 192, 192),
    A256CBC("A256CBC", "AES/CBC/ZeroBytePadding", KeyType.OCT, 256, 256),
    A256CBCPAD("A256CBCPAD", "AES/CBC/PKCS5Padding", KeyType.OCT, 256, 256),
    RSA_OAEP("RSA-OAEP", "RSA/None/OAEPWithSHA1AndMGF1Padding", KeyType.RSA, 2048, 4096),
    RSA_OAEP_256("RSA-OAEP-256", "RSA/None/OAEPWithSHA256AndMGF1Padding", KeyType.RSA, 2048, 4096),
    RSA1_5("RSA1_5", "RSA/None/PKCS1Padding", KeyType.RSA, 2048, 4096);

    private final String value;
    private final String alg;
    private final KeyType compatibleType;
    private final int minKeySize;
    private final int maxKeySize;

    EncryptionAlgorithm(final String value,
                        final String alg, final KeyType compatibleType,
                        final int minKeySize, final int maxKeySize) {
        this.value = value;
        this.alg = alg;
        this.compatibleType = compatibleType;
        this.minKeySize = minKeySize;
        this.maxKeySize = maxKeySize;
    }

    @JsonCreator
    public static EncryptionAlgorithm forValue(final String name) {
        return Arrays.stream(values()).filter(keyType -> keyType.getValue().equals(name)).findFirst().orElse(null);
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
}
