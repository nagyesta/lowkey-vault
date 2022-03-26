package com.github.nagyesta.lowkeyvault.model.v7_2.secret;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SecretBackupListItem extends BaseBackupListItem<SecretPropertiesModel> {
    @NotNull
    @JsonProperty("value")
    private String value;
    @JsonProperty("contentType")
    private String contentType;

}
