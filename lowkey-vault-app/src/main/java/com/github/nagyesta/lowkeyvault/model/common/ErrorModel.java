package com.github.nagyesta.lowkeyvault.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public record ErrorModel(@JsonProperty("error") ErrorMessage error) {

    public static ErrorModel fromException(final Exception e) {
        return new ErrorModel(Objects.requireNonNull(ErrorMessage.fromException(e)));
    }
}
