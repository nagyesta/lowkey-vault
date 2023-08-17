package com.github.nagyesta.lowkeyvault.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorModel(@JsonProperty("error") ErrorMessage error) {
    public ErrorModel(final ErrorMessage error) {
        this.error = error;
    }

    public static ErrorModel fromException(final Exception e) {
        return new ErrorModel(ErrorMessage.fromException(e));
    }
}
