package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import org.springframework.lang.NonNull;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;

/**
 * Converts import requests to EC key pairs.
 */
public class EcJsonWebKeyImportRequestConverter extends BaseJsonWebKeyImportRequestConverter<KeyPair, KeyCurveName> {

    @NonNull
    @Override
    public KeyPair convert(@NonNull final JsonWebKeyImportRequest source) {
        try {
            final var spec = parameterSpec(source);
            final var factory = KeyFactory.getInstance(source.getKeyType().getAlgorithmName(),
                    KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            final var privateKey = factory.generatePrivate(ecPrivateKeySpec(spec, source));
            final var publicKey = factory.generatePublic(ecPublicKeySpec(spec, source));
            return new KeyPair(publicKey, privateKey);
        } catch (final Exception e) {
            throw new CryptoException(e.getMessage(), e);
        }
    }

    @Override
    public KeyCurveName getKeyParameter(@NonNull final JsonWebKeyImportRequest source) {
        return source.getCurveName();
    }

    private ECPublicKeySpec ecPublicKeySpec(final ECParameterSpec spec, final JsonWebKeyImportRequest source) {
        final var ecPoint = new ECPoint(asInt(source.getX()), asInt(source.getY()));
        return new ECPublicKeySpec(ecPoint, spec);
    }

    private ECPrivateKeySpec ecPrivateKeySpec(final ECParameterSpec spec, final JsonWebKeyImportRequest source) {
        return new ECPrivateKeySpec(asInt(source.getD()), spec);
    }

    private ECParameterSpec parameterSpec(final JsonWebKeyImportRequest source) {
        return ((ECPublicKey) KeyGenUtil.generateEc(source.getCurveName()).getPublic()).getParams();
    }
}
