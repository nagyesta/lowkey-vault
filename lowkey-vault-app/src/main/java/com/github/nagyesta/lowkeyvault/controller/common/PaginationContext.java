package com.github.nagyesta.lowkeyvault.controller.common;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_PREFIX;

@Data
@Builder
class PaginationContext {
    private static final String AND = "&";
    private static final String EQUALS = "=";
    private static final String QUESTION_MARK = "?";
    private static final String EMPTY = "";
    @NonNull
    private final URI base;
    private final int totalItems;
    private final int currentItems;
    private final int limit;
    private final int offset;
    private Map<String, String> additionalParameters;
    @NonNull
    private final String apiVersion;

    public URI asNextUri() {
        URI nextUri = null;
        if (hasMorePages()) {
            nextUri = URI.create(base
                    + QUESTION_MARK + API_VERSION_PREFIX + apiVersion
                    + AND + GenericEntityController.SKIP_TOKEN_PARAM + EQUALS + skipValue()
                    + AND + GenericEntityController.MAX_RESULTS_PARAM + EQUALS + limit
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
