package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import lombok.NonNull;
import org.springframework.core.convert.converter.Converter;

import java.security.interfaces.RSAPrivateCrtKey;

public class RsaPrivateKeyToJsonWebKeyImportRequestConverter
        implements Converter<RSAPrivateCrtKey, JsonWebKeyImportRequest> {

    @Override
    public JsonWebKeyImportRequest convert(final @NonNull RSAPrivateCrtKey source) {
        final var importRequest = new JsonWebKeyImportRequest();
        importRequest.setKeyType(KeyType.RSA);
        importRequest.setN(source.getModulus().toByteArray());
        importRequest.setE(source.getPublicExponent().toByteArray());
        importRequest.setD(source.getPrivateExponent().toByteArray());
        importRequest.setP(source.getPrimeP().toByteArray());
        importRequest.setQ(source.getPrimeQ().toByteArray());
        importRequest.setDq(source.getPrimeExponentQ().toByteArray());
        importRequest.setDp(source.getPrimeExponentP().toByteArray());
        importRequest.setQi(source.getCrtCoefficient().toByteArray());
        return importRequest;
    }
}
