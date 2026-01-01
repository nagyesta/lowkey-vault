package com.github.nagyesta.lowkeyvault.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.openapi.Examples.ERROR_MESSAGE;
import static com.github.nagyesta.lowkeyvault.openapi.Examples.EXCEPTION;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    @Schema(example = EXCEPTION, description = "The class of the exception caused.")
    @JsonProperty("code")
    private String code;
    @Nullable
    @Hidden
    @JsonProperty("innererror")
    private ErrorMessage innerError;
    @Schema(example = ERROR_MESSAGE, description = "The human readable message of the exception.")
    @JsonProperty("message")
    private String message;

    public static @Nullable ErrorMessage fromException(@Nullable final Throwable t) {
        return Optional.ofNullable(t)
                .map(throwable -> new ErrorMessage(
                        throwable.getClass().getName(),
                        ErrorMessage.fromException(throwable.getCause()),
                        throwable.getMessage()))
                .orElse(null);
    }
}
