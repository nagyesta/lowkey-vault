package com.github.nagyesta.lowkeyvault.controller.common;

import lombok.Builder;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_PREFIX;

@Data
@Builder
public class PaginationContext {

    /**
     * Default page size used when returning available versions of an entity.
     */
    public static final String DEFAULT_MAX = "25";
    /**
     * Default offset used when returning available versions of an entity.
     */
    public static final String SKIP_ZERO = "0";
    /**
     * Parameter name for the page size when returning versions of an entity.
     */
    public static final String MAX_RESULTS_PARAM = "maxresults";
    /**
     * Parameter name for the offset when returning versions of an entity.
     */
    public static final String SKIP_TOKEN_PARAM = "$skiptoken";
    private static final String AND = "&";
    private static final String EQUALS = "=";
    private static final String QUESTION_MARK = "?";
    private static final String EMPTY = "";
    private final URI base;
    private final int totalItems;
    private final int currentItems;
    private final int limit;
    private final int offset;
    @Nullable
    private Map<String, String> additionalParameters;
    private final String apiVersion;

    public @Nullable URI asNextUri() {
        URI nextUri = null;
        if (hasMorePages()) {
            nextUri = URI.create(base
                    + QUESTION_MARK + API_VERSION_PREFIX + apiVersion
                    + AND + SKIP_TOKEN_PARAM + EQUALS + skipValue()
                    + AND + MAX_RESULTS_PARAM + EQUALS + limit
                    + additionalParametersAsQuery());
        }
        return nextUri;
    }

    private boolean hasMorePages() {
        return limit + offset < totalItems;
    }

    private int skipValue() {
        return offset + currentItems;
    }

    private String additionalParametersAsQuery() {
        return Optional.ofNullable(additionalParameters)
                .map(m -> AND + m.entrySet().stream()
                        .map(e -> e.getKey() + EQUALS + e.getValue())
                        .collect(Collectors.joining(AND)))
                .orElse(EMPTY);
    }
}
