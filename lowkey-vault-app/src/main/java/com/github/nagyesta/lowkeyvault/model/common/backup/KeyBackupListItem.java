package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KeyBackupListItem extends BaseBackupListItem<KeyPropertiesModel> {
    @Valid
    @NotNull
    @JsonProperty("keyMaterial")
    private JsonWebKeyImportRequest keyMaterial;
}
