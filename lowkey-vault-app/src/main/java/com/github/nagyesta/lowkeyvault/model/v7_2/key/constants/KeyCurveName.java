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
    P_256("P-256", "secp256r1", Set.of("prime256v1")),
    /**
     * P-384.
     */
    P_384("P-384", "secp384r1", Set.of()),
    /**
     * P-521.
     */
    P_521("P-521", "secp521r1", Set.of()),
    /**
     * P-256K.
     */
    P_256K("P-256K", "secp256k1", Set.of());

    private final String value;
    private final String alg;
    private final Set<String> equivalentAlgs;

    KeyCurveName(final String value, final String alg, final Set<String> equivalentAlgs) {
        this.value = value;
        this.alg = alg;
        final TreeSet<String> algSet = new TreeSet<>(equivalentAlgs);
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
}
