package com.github.nagyesta.lowkeyvault.model.v7_2.secret.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@Data
public class UpdateSecretRequest {

    @Nullable
    @JsonProperty("contentType")
    private String contentType;
    @Nullable
    @JsonProperty("attributes")
    private BasePropertiesUpdateModel properties;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags;

}
