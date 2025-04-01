package com.github.nagyesta.lowkeyvault.mapper.common;

import com.github.nagyesta.lowkeyvault.context.ApiVersionAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.converter.Converter;

public interface ApiVersionAwareConverter<S, T>
        extends Converter<S, T>, ApiVersionAware, InitializingBean {
}
