package com.github.nagyesta.lowkeyvault.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {
    @JsonProperty("code")
    private String code;
    @JsonProperty("innererror")
    private ErrorMessage innerError;
    @JsonProperty("message")
    private String message;

    static ErrorMessage fromException(final Throwable t) {
        return Optional.ofNullable(t)
                .map(throwable -> new ErrorMessage(
                        throwable.getClass().getName(),
                        ErrorMessage.fromException(throwable.getCause()),
                        throwable.getMessage()))
                .orElse(null);
    }
}
