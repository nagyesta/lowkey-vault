package com.github.nagyesta.lowkeyvault.model.v7_2.secret.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Data
public class CreateSecretRequest {

    @Nullable
    @JsonProperty("contentType")
    private String contentType;
    @NotNull
    @NotBlank
    @JsonProperty("value")
    private String value;
    @Nullable
    @Valid
    @JsonProperty("attributes")
    private SecretPropertiesModel properties;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags = Map.of();

}
