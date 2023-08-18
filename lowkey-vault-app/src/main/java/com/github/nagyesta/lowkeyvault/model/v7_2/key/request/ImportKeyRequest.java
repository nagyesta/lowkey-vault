package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class ImportKeyRequest {

    @NotNull
    @Valid
    @JsonProperty("key")
    private JsonWebKeyImportRequest key;

    @JsonProperty("attributes")
    private BasePropertiesUpdateModel properties;

    @JsonProperty("Hsm")
    private Boolean hsm;

    @JsonProperty("tags")
    private Map<String, String> tags;

}
