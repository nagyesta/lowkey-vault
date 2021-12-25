package com.github.nagyesta.lowkeyvault.model.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ErrorModel {
    @JsonProperty("error")
    private final ErrorMessage error;

    public ErrorModel(final ErrorMessage error) {
        this.error = error;
    }

    public static ErrorModel fromException(final Exception e) {
        return new ErrorModel(ErrorMessage.fromException(e));
    }
}
