package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.HashAlgorithm;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtil {

    private HashUtil() {
    }

    public static byte[] hash(final byte[] data, final HashAlgorithm algorithm) {
        try {
            return hash(data, algorithm.getAlgorithmName());
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static byte[] hash(final byte[] data, final String algorithm) throws NoSuchAlgorithmException {
        final var md = MessageDigest.getInstance(algorithm);
        md.update(data);
        return md.digest();
    }
}
