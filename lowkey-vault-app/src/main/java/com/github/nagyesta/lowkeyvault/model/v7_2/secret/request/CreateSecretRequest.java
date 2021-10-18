package com.github.nagyesta.lowkeyvault.model.v7_2.secret.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretPropertiesModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class CreateSecretRequest {

    @JsonProperty("contentType")
    private String contentType;

    @NotNull
    @NotBlank
    @JsonProperty("value")
    private String value;

    @JsonProperty("attributes")
    private SecretPropertiesModel properties;

    @JsonProperty("tags")
    private Map<String, String> tags = Map.of();

}
