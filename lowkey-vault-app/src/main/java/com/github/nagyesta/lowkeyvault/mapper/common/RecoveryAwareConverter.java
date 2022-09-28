package com.github.nagyesta.lowkeyvault.mapper.common;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.net.URI;

/**
 * Converter interface supporting both active and deleted entities.
 *
 * @param <S>  The source type.
 * @param <T>  The active target type.
 * @param <DT> The deleted target type.
 */
public interface RecoveryAwareConverter<S, T, DT extends T> {

    @Nullable
    T convert(S source, @NonNull URI vaultUri);

    @NonNull
    DT convertDeleted(@NonNull S source, @NonNull URI vaultUri);
}
