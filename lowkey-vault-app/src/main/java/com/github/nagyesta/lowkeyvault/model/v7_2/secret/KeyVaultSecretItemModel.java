package com.github.nagyesta.lowkeyvault.model.v7_2.secret;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyVaultSecretItemModel {

    @JsonProperty("id")
    private String id;
    @JsonProperty("contentType")
    private String contentType;
    @JsonProperty("attributes")
    private SecretPropertiesModel attributes;
    @JsonProperty("tags")
    private Map<String, String> tags;
    @JsonProperty("managed")
    private Boolean managed;

}
