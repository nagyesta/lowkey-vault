package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public enum KeyCurveName {

    /**
     * P-256.
     */
    P_256("P-256", "secp256r1", Set.of("prime256v1"), 256),
    /**
     * P-384.
     */
    P_384("P-384", "secp384r1", Set.of(), 384),
    /**
     * P-521.
     */
    P_521("P-521", "secp521r1", Set.of(), 521),
    /**
     * P-256K.
     */
    P_256K("P-256K", "secp256k1", Set.of(), 256);

    private static final double BITS_PER_BYTE = 8.0D;
    private final String value;
    private final String alg;

    private final int bitLength;
    private final Set<String> equivalentAlgs;

    KeyCurveName(final String value, final String alg, final Set<String> equivalentAlgs, final int bitLength) {
        this.value = value;
        this.alg = alg;
        this.bitLength = bitLength;
        final var algSet = new TreeSet<String>(equivalentAlgs);
        algSet.add(alg);
        this.equivalentAlgs = algSet;
    }

    @JsonCreator
    public static KeyCurveName forValue(final String name) {
        return Arrays.stream(values()).filter(keyType -> keyType.getValue().equals(name)).findFirst().orElse(null);
    }

    public static KeyCurveName forAlg(final String name) {
        return Arrays.stream(values()).filter(keyType -> keyType.equivalentAlgs.contains(name)).findFirst().orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonIgnore
    public AlgorithmParameterSpec getAlgSpec() {
        return new ECGenParameterSpec(alg);
    }

    @JsonIgnore
    public int getBitLength() {
        return bitLength;
    }

    @JsonIgnore
    public int getByteLength() {
        return (int) Math.ceil(getBitLength() / BITS_PER_BYTE);
    }
}
