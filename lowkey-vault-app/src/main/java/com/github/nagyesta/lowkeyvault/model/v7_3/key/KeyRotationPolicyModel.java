package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Restore;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.net.URI;
import java.util.List;

@Data
@JsonPropertyOrder({"id", "lifetimeActions", "attributes"})
public class KeyRotationPolicyModel {

    @NotNull(groups = {Restore.class})
    @JsonProperty("id")
    private URI id;
    @Valid
    @NotNull(groups = {Restore.class, Update.class})
    @JsonProperty("attributes")
    private KeyRotationPolicyAttributes attributes;
    @Valid
    @NotNull(groups = {Restore.class, Update.class})
    @Size(min = 1, max = 2, groups = {Restore.class, Update.class})
    @JsonProperty("lifetimeActions")
    private List<KeyLifetimeActionModel> lifetimeActions;
    @JsonIgnore
    private KeyEntityId keyEntityId;

}
