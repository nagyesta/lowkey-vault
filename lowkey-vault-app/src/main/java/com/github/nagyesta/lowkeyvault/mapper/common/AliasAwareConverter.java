package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.net.URI;

/**
 * A converter that converts a source object of type {@code S} to a target of type {@code T}
 * while replacing vault base URI  references in Ids.
 *
 * @param <S> the source type
 * @param <T> the target type
 */
public interface AliasAwareConverter<S, T>
        extends ApiVersionAware, InitializingBean {

    @Nullable
    T convert(S source, @NonNull URI vaultUri);
}
