package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

public enum KeyCurveName {

    /**
     * P-256.
     */
    P_256("P-256", "secp256r1"),
    /**
     * P-384.
     */
    P_384("P-384", "secp384r1"),
    /**
     * P-521.
     */
    P_521("P-521", "secp521r1"),
    /**
     * P-256K.
     */
    P_256K("P-256K", "secp256k1");

    private final String value;
    private final String alg;

    KeyCurveName(final String value, final String alg) {
        this.value = value;
        this.alg = alg;
    }

    @JsonCreator
    public static KeyCurveName forValue(final String name) {
        return Arrays.stream(values()).filter(keyType -> keyType.getValue().equals(name)).findFirst().orElse(null);
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonIgnore
    public AlgorithmParameterSpec getAlgSpec() {
        return new ECGenParameterSpec(alg);
    }
}
