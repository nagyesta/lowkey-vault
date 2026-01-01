package com.github.nagyesta.lowkeyvault.mapper.common;

import org.jspecify.annotations.Nullable;

public interface BaseConverter<S, T> {

    @Nullable T convert(@Nullable S source);
}
