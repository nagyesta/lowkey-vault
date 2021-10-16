package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyVaultKeyItemModel {
    @JsonProperty("kid")
    private String keyId;

    @JsonProperty("attributes")
    private KeyPropertiesModel attributes;

    @JsonProperty("tags")
    private Map<String, String> tags;

    @JsonProperty("managed")
    private Boolean managed;

}
