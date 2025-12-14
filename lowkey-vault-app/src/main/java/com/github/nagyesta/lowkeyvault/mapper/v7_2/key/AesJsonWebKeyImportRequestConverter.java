package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Objects;

/**
 * Converts import requests to AES key pairs.
 */
public class AesJsonWebKeyImportRequestConverter
        extends BaseJsonWebKeyImportRequestConverter<SecretKey, Integer> {

    private static final int AES_BYTES_TO_KEY_SIZE_BITS_MULTIPLIER = 8;

    @Override
    public SecretKey convert(final JsonWebKeyImportRequest source) {
        try {
            return new SecretKeySpec(Objects.requireNonNull(source.getK()), source.getKeyType().getAlgorithmName());
        } catch (final Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Override
    public Integer getKeyParameter(final JsonWebKeyImportRequest source) {
        return Objects.requireNonNull(source.getK()).length * AES_BYTES_TO_KEY_SIZE_BITS_MULTIPLIER;
    }

}
