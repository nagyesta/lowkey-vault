package com.github.nagyesta.lowkeyvault.model.v7_2.key.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Arrays;

@SuppressWarnings("checkstyle:JavadocVariable")
public enum SignatureAlgorithm {

    ES256("ES256", "NONEwithECDSA", KeyType.EC, HashAlgorithm.SHA256) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_256 == keyCurveName;
        }
    },
    ES256K("ES256K", "NONEwithECDSA", KeyType.EC, HashAlgorithm.SHA256) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_256K == keyCurveName;
        }
    },
    ES384("ES384", "NONEwithECDSA", KeyType.EC, HashAlgorithm.SHA384) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_384 == keyCurveName;
        }
    },
    ES512("ES512", "NONEwithECDSA", KeyType.EC, HashAlgorithm.SHA512) {
        @Override
        public boolean isCompatibleWithCurve(final KeyCurveName keyCurveName) {
            return KeyCurveName.P_521 == keyCurveName;
        }
    },
    PS256("PS256", "NONEwithRSAandMGF1", KeyType.RSA, HashAlgorithm.SHA256) {
        @Override
        public Signature getSignatureInstance() throws GeneralSecurityException {
            final Signature signature = super.getSignatureInstance();
            signature.setParameter(getHashAlgorithm().getPssParameter());
            return signature;
        }
    },
    PS384("PS384", "NONEwithRSAandMGF1", KeyType.RSA, HashAlgorithm.SHA384) {
        @Override
        public Signature getSignatureInstance() throws GeneralSecurityException {
            final Signature signature = super.getSignatureInstance();
            signature.setParameter(getHashAlgorithm().getPssParameter());
            return signature;
        }
    },
    PS512("PS512", "NONEwithRSAandMGF1", KeyType.RSA, HashAlgorithm.SHA512) {
        @Override
        public Signature getSignatureInstance() throws GeneralSecurityException {
            final Signature signature = super.getSignatureInstance();
            signature.setParameter(getHashAlgorithm().getPssParameter());
            return signature;
        }
    },
    RS256("RS256", "NoneWithRSA", KeyType.RSA, HashAlgorithm.SHA256) {
        @Override
        public byte[] transformDigest(final byte[] digest) throws IOException {
            return getHashAlgorithm().encodeDigest(digest);
        }
    },
    RS384("RS384", "NoneWithRSA", KeyType.RSA, HashAlgorithm.SHA384) {
        @Override
        public byte[] transformDigest(final byte[] digest) throws IOException {
            return getHashAlgorithm().encodeDigest(digest);
        }
    },
    RS512("RS512", "NoneWithRSA", KeyType.RSA, HashAlgorithm.SHA512) {
        @Override
        public byte[] transformDigest(final byte[] digest) throws IOException {
            return getHashAlgorithm().encodeDigest(digest);
        }
    };

    private final String value;
    private final String alg;
    private final KeyType compatibleType;
    private final HashAlgorithm hashAlgorithm;

    SignatureAlgorithm(final String value,
                       final String alg,
                       final KeyType compatibleType,
                       final HashAlgorithm hashAlgorithm) {
        this.value = value;
        this.alg = alg;
        this.compatibleType = compatibleType;
        this.hashAlgorithm = hashAlgorithm;
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
    public byte[] transformDigest(final byte[] digest) throws IOException {
        return digest;
    }

    @JsonIgnore
    public HashAlgorithm getHashAlgorithm() {
        return hashAlgorithm;
    }

    @JsonIgnore
    public Signature getSignatureInstance() throws GeneralSecurityException {
        return Signature.getInstance(getAlg(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
    }
}
