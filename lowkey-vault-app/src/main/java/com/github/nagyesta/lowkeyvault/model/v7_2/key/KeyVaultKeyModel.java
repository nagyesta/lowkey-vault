package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyVaultKeyModel {

    @JsonProperty("key")
    private JsonWebKeyModel key;
    @Nullable
    @JsonProperty("attributes")
    private KeyPropertiesModel attributes;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags;
    @Nullable
    @JsonProperty("managed")
    private Boolean managed;

}
