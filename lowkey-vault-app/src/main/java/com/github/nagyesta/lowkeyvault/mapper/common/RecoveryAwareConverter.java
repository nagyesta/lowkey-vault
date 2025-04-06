package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import org.springframework.beans.factory.InitializingBean;
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
@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public interface RecoveryAwareConverter<S, T, DT extends T>
        extends ApiVersionAware, InitializingBean {

    @Nullable
    T convert(S source, @NonNull URI vaultUri);

    @NonNull
    DT convertDeleted(@NonNull S source, @NonNull URI vaultUri);
}
