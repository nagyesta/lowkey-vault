package com.github.nagyesta.lowkeyvault.mapper.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;

/**
 * Converter interface supporting both active and deleted entities.
 *
 * @param <S>  The source type.
 * @param <T>  The active target type.
 * @param <DT> The deleted target type.
 */
public interface RecoveryAwareConverter<S, T, DT extends T> extends Converter<S, T> {

    @NonNull
    DT convertDeleted(@NonNull S source);
}
