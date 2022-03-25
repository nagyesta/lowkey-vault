package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KeyBackupListItem extends BaseBackupListItem<KeyPropertiesModel> {
    @Valid
    @NotNull
    @JsonProperty("keyMaterial")
    private JsonWebKeyImportRequest keyMaterial;
}
