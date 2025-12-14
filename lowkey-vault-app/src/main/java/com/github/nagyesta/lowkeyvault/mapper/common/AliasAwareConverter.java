package com.github.nagyesta.lowkeyvault.mapper.common;

import org.jspecify.annotations.Nullable;

import java.net.URI;

/**
 * A converter that converts a source object of type {@code S} to a target of type {@code T}
 * while replacing vault base URI  references in Ids.
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public interface AliasAwareConverter<S, T> {

    @Nullable
    T convert(@Nullable S source, URI vaultUri);
}
