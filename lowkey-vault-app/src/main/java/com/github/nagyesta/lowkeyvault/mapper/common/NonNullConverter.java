package com.github.nagyesta.lowkeyvault.mapper.common;

public interface NonNullConverter<S, T> {

    T convert(S source);
}
