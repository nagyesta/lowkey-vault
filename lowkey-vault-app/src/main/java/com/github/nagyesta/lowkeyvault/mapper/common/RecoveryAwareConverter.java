package com.github.nagyesta.lowkeyvault.mapper.common;

import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * Converter interface supporting both active and deleted entities.
 *
 * @param <S>  The source type.
 * @param <T>  The active target type.
 * @param <DT> The deleted target type.
 */
@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public interface RecoveryAwareConverter<S, T, DT extends T> {

    @Nullable
    T convert(@Nullable S source, URI vaultUri);

    @Nullable
    DT convertDeleted(@Nullable S source, URI vaultUri);
}
