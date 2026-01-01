package com.github.nagyesta.lowkeyvault.model.v7_2.secret;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyVaultSecretItemModel {

    @JsonProperty("id")
    private String id;
    @Nullable
    @JsonProperty("contentType")
    private String contentType;
    @Nullable
    @JsonProperty("attributes")
    private SecretPropertiesModel attributes;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags;
    @Nullable
    @JsonProperty("managed")
    private Boolean managed;

}
