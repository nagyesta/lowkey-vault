package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

import java.net.URI;
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

    public KeyVaultKeyItemModel(@NonNull final KeyPropertiesModel attributes,
                                @NonNull final URI keyUri,
                                @NonNull final Map<String, String> tags) {
        this.attributes = attributes;
        this.keyId = keyUri.toString();
        this.tags = Map.copyOf(tags);
    }
}
