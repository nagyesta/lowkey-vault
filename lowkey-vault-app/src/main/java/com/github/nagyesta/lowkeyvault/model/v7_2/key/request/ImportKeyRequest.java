package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.util.Map;

@SuppressWarnings("checkstyle:MagicNumber")
@Data
public class ImportKeyRequest {

    @NotNull
    @Valid
    @JsonProperty("key")
    private JsonWebKeyImportRequest key;
    @Nullable
    @Valid
    @JsonProperty("attributes")
    private BasePropertiesUpdateModel properties;
    @Nullable
    @JsonProperty("Hsm")
    private Boolean hsm;
    @Nullable
    @JsonProperty("tags")
    private Map<String, String> tags = Map.of();

}
