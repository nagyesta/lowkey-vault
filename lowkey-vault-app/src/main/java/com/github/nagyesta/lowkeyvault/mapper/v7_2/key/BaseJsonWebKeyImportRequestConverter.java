package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import org.springframework.core.convert.converter.Converter;

import java.math.BigInteger;

/**
 * Base class for converting import requests to keys or key pairs.
 *
 * @param <T> return type.
 * @param <P> the input parameter (key size or curve name)
 */
public abstract class BaseJsonWebKeyImportRequestConverter<T, P> implements Converter<JsonWebKeyImportRequest, T> {

    public abstract P getKeyParameter(JsonWebKeyImportRequest request);

    protected BigInteger asInt(final byte[] inBytes) {
        return new BigInteger(1, inBytes);
    }
}
