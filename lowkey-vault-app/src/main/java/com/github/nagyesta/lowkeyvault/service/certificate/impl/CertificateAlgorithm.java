package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

public enum CertificateAlgorithm {

    /**
     * Certificate signed using RSA algorithm.
     */
    RSA("SHA256withRSA", Set.of(KeyType.RSA, KeyType.RSA_HSM)),
    /**
     * Certificate signed using EC algorithm.
     */
    EC("SHA256withECDSA", Set.of(KeyType.EC, KeyType.EC_HSM));

    @Getter
    private final String algorithm;
    private final Set<KeyType> keyTypes;

    CertificateAlgorithm(
            final String algorithm,
            final Set<KeyType> keyTypes) {
        this.algorithm = algorithm;
        this.keyTypes = keyTypes;
    }

    public static CertificateAlgorithm forKeyType(final KeyType keyType) {
        final var value = Arrays.stream(CertificateAlgorithm.values())
                .filter(v -> v.keyTypes.contains(keyType))
                .findFirst();
        return value.orElseThrow(() -> new IllegalArgumentException("Unable to find certificate algorithm for key type: " + keyType));
    }
}
