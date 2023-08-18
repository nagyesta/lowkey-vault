package com.github.nagyesta.lowkeyvault.model.management;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VaultBackupListModel {
    @Valid
    @NotNull
    @Size(min = 1)
    @JsonProperty("vaults")
    private List<VaultBackupModel> vaults;
}
