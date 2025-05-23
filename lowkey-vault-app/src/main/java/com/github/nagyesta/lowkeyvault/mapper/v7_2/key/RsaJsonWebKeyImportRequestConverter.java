package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import org.springframework.lang.NonNull;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * Converts import requests to RSA key pairs.
 */
public class RsaJsonWebKeyImportRequestConverter
        extends BaseJsonWebKeyImportRequestConverter<KeyPair, Integer> {

    private static final int RSA_MODULUS_BYTES_TO_KEY_SIZE_BITS_MULTIPLIER = 8;

    @NonNull
    @Override
    public KeyPair convert(@NonNull final JsonWebKeyImportRequest source) {
        try {
            final var factory = KeyFactory.getInstance(source.getKeyType().getAlgorithmName());
            final var privateKey = factory.generatePrivate(rsaPrivateKeySpec(source));
            final var publicKey = factory.generatePublic(rsaPublicKeySpec(source));
            return new KeyPair(publicKey, privateKey);
        } catch (final Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Override
    public Integer getKeyParameter(@NonNull final JsonWebKeyImportRequest source) {
        final var calculatedWithPotentialLeadingZero = source.getN().length * RSA_MODULUS_BYTES_TO_KEY_SIZE_BITS_MULTIPLIER;
        final var validValuesBelowLimit = KeyType.RSA.getValidKeyParameters(Integer.class)
                .headSet(calculatedWithPotentialLeadingZero + 1);
        return validValuesBelowLimit.last();
    }

    private RSAPublicKeySpec rsaPublicKeySpec(final JsonWebKeyImportRequest source) {
        return new RSAPublicKeySpec(asInt(source.getN()), asInt(source.getE()));
    }

    private RSAPrivateKeySpec rsaPrivateKeySpec(final JsonWebKeyImportRequest source) {
        return new RSAPrivateCrtKeySpec(
                asInt(source.getN()), asInt(source.getE()), asInt(source.getD()), asInt(source.getP()),
                asInt(source.getQ()), asInt(source.getDp()), asInt(source.getDq()), asInt(source.getQi()));
    }
}
