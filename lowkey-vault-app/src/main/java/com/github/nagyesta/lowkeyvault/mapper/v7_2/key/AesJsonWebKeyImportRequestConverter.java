package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import org.springframework.lang.NonNull;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Converts import requests to AES key pairs.
 */
public class AesJsonWebKeyImportRequestConverter
        extends BaseJsonWebKeyImportRequestConverter<SecretKey, Integer> {

    private static final int AES_BYTES_TO_KEY_SIZE_BITS_MULTIPLIER = 8;

    @NonNull
    @Override
    public SecretKey convert(@NonNull final JsonWebKeyImportRequest source) {
        try {
            return new SecretKeySpec(source.getK(), source.getKeyType().getAlgorithmName());
        } catch (final Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Override
    public Integer getKeyParameter(@NonNull final JsonWebKeyImportRequest source) {
        return source.getK().length * AES_BYTES_TO_KEY_SIZE_BITS_MULTIPLIER;
    }

}
