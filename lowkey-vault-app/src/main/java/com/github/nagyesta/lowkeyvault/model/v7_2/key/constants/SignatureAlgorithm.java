package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

@SuppressWarnings("checkstyle:JavadocVariable")
public enum SignatureAlgorithm {

    ES256("ES256", "NONEwithECDSAinP1363Format", KeyType.EC) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_256 == keyCurveName;
        }

        @SuppressWarnings("checkstyle:MagicNumber")
        @Override
        public boolean supportsDigestLength(final int digestLength) {
            return digestLength == 32;
        }
    },
    ES256K("ES256K", "NONEwithECDSAinP1363Format", KeyType.EC) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_256K == keyCurveName;
        }

        @SuppressWarnings("checkstyle:MagicNumber")
        @Override
        public boolean supportsDigestLength(final int digestLength) {
            return digestLength == 32;
        }
    },
    ES384("ES384", "NONEwithECDSAinP1363Format", KeyType.EC) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_384 == keyCurveName;
        }

        @SuppressWarnings("checkstyle:MagicNumber")
        @Override
        public boolean supportsDigestLength(final int digestLength) {
            return digestLength == 48;
        }
    },
    ES512("ES512", "NONEwithECDSAinP1363Format", KeyType.EC) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_521 == keyCurveName;
        }

        @SuppressWarnings("checkstyle:MagicNumber")
        @Override
        public boolean supportsDigestLength(final int digestLength) {
            return digestLength == 64;
        }
    },
    PS256("PS256", "SHA256withRSAandMGF1", KeyType.RSA),
    PS384("PS384", "SHA384withRSAandMGF1", KeyType.RSA),
    PS512("PS512", "SHA512withRSAandMGF1", KeyType.RSA),
    RS256("RS256", "SHA256withRSA", KeyType.RSA),
    RS384("RS384", "SHA384withRSA", KeyType.RSA),
    RS512("RS512", "SHA512withRSA", KeyType.RSA);

    private final String value;
    private final String alg;
    private final KeyType compatibleType;

    SignatureAlgorithm(final String value,
                       final String alg, final KeyType compatibleType) {
        this.value = value;
        this.alg = alg;
        this.compatibleType = compatibleType;
    }

    @JsonCreator
    public static SignatureAlgorithm forValue(final String name) {
        return Arrays.stream(values()).filter(algorithm -> algorithm.getValue().equals(name)).findFirst().orElse(null);
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
    public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
        return false;
    }

    @JsonIgnore
    public boolean supportsDigestLength(final int digestLength) {
        return false;
    }

}
