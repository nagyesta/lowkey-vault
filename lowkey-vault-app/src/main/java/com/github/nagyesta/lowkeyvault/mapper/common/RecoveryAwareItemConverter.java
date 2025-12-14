package com.github.nagyesta.lowkeyvault.mapper.common;

import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * Converter interface supporting both active and deleted entities, as well as items without entity versions.
 *
 * @param <S>  The source type.
 * @param <T>  The active target type.
 * @param <DT> The deleted target type.
 */
@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public interface RecoveryAwareItemConverter<S, T, DT extends T>
        extends RecoveryAwareConverter<S, T, DT> {

    @Nullable
    T convertWithoutVersion(@Nullable S source, URI vaultUri);
}
