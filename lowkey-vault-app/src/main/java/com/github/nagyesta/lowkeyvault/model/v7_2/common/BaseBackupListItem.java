package com.github.nagyesta.lowkeyvault.model.v7_2.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.net.URI;
import java.util.Map;

/**
 * Base list item of backup models.
 *
 * @param <P> The type of the properties model.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseBackupListItem<P extends BasePropertiesModel> {

    @NotNull
    @JsonProperty("vaultBaseUri")
    private URI vaultBaseUri;
    @NotNull
    @NotBlank
    @JsonProperty("entityId")
    private String id;
    @NotNull
    @NotBlank
    @JsonProperty("entityVersion")
    private String version;
    @Valid
    @NotNull
    @JsonProperty("attributes")
    private P attributes;
    @JsonProperty("tags")
    private Map<String, String> tags;
    @JsonProperty("managed")
    private boolean managed;
}
