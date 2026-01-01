package com.github.nagyesta.lowkeyvault.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.JsonWebKeyModel;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

public record KeyDetails(@JsonProperty("keys") List<JsonWebKeyModel> keys) {

    public KeyDetails(final KeyPair keyPair) {
        this(List.of(convertKey(keyPair)));
    }

    private static JsonWebKeyModel convertKey(final KeyPair keyPair) {
        final var publicKey = (RSAPublicKey) keyPair.getPublic();
        final var privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        final var keyModel = new JsonWebKeyModel();
        keyModel.setN(publicKey.getModulus().toByteArray());
        keyModel.setE(publicKey.getPublicExponent().toByteArray());
        keyModel.setD(privateKey.getPrivateExponent().toByteArray());
        keyModel.setDp(privateKey.getPrimeExponentP().toByteArray());
        keyModel.setDq(privateKey.getPrimeExponentQ().toByteArray());
        keyModel.setP(privateKey.getPrimeP().toByteArray());
        keyModel.setQ(privateKey.getPrimeQ().toByteArray());
        keyModel.setQi(privateKey.getCrtCoefficient().toByteArray());
        keyModel.setKeyOps(null);
        keyModel.setKeyType(null);
        keyModel.setKeyHsm(null);
        return keyModel;
    }

}
