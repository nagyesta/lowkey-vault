package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Restore;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
import lombok.Data;
import lombok.NonNull;

import javax.validation.constraints.NotNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyLifetimeActionTypeModel {
    @NotNull(groups = {Restore.class, Update.class})
    @JsonProperty("type")
    private LifetimeActionType type;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public KeyLifetimeActionTypeModel() {
    }

    public KeyLifetimeActionTypeModel(@NonNull final LifetimeActionType type) {
        this();
        this.type = type;
    }
}
