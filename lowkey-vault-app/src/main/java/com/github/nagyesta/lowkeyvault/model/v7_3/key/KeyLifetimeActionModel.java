package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Restore;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"trigger", "action"})
public class KeyLifetimeActionModel {

    @NotNull(groups = {Restore.class, Update.class})
    @Valid
    @JsonProperty("trigger")
    private KeyLifetimeActionTriggerModel trigger;
    @NotNull(groups = {Restore.class, Update.class})
    @Valid
    @JsonProperty("action")
    private KeyLifetimeActionTypeModel action;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public KeyLifetimeActionModel() {
    }

    public KeyLifetimeActionModel(final KeyLifetimeActionTypeModel action, final KeyLifetimeActionTriggerModel trigger) {
        this();
        this.action = action;
        this.trigger = trigger;
    }
}
