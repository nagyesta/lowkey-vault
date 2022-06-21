package com.github.nagyesta.lowkeyvault.model.management;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretBackupList;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyBackupList;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VaultBackupModel {
    @Valid
    @NotNull
    @JsonProperty("attributes")
    private VaultModel attributes;
    @Valid
    @NotNull
    @JsonProperty("keys")
    private Map<String, KeyBackupList> keys;
    @Valid
    @NotNull
    @JsonProperty("secrets")
    private Map<String, SecretBackupList> secrets;
}
