package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URI;
import java.util.List;

@Data
@JsonPropertyOrder({"id", "lifetimeActions", "attributes"})
public class KeyRotationPolicyModel {

    @JsonProperty("id")
    private URI id;
    @Valid
    @NotNull
    @JsonProperty("attributes")
    private KeyRotationPolicyAttributes attributes;
    @Valid
    @NotNull
    @Size(min = 1, max = 2)
    @JsonProperty("lifetimeActions")
    private List<KeyLifetimeActionModel> lifetimeActions;

}
