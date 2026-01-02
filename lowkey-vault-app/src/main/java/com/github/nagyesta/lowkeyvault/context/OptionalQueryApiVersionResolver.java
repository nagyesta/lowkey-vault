package com.github.nagyesta.lowkeyvault.context;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.Nullable;
import org.springframework.web.accept.QueryApiVersionResolver;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_NAME;

/**
 * Query parameter-based version resolver that ignores versioning in case of metadata requests.
 */
public class OptionalQueryApiVersionResolver extends QueryApiVersionResolver {

    public OptionalQueryApiVersionResolver() {
        super(API_VERSION_NAME);
    }

    @Override
    public @Nullable String resolveVersion(final HttpServletRequest request) {
        //ignore versioning in case of metadata requests
        if (!request.isSecure()) {
            return null;
        }
        return super.resolveVersion(request);
    }
}
