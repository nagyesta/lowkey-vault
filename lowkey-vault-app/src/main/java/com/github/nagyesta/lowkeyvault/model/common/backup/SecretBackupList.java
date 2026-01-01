package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

@Getter
@EqualsAndHashCode
public class SecretBackupList
        implements BackupListContainer<SecretBackupListItem> {

    @NotNull
    @Size(min = 1)
    @JsonProperty("versions")
    private List<@Valid SecretBackupListItem> versions = List.of();

    public void setVersions(final List<SecretBackupListItem> versions) {
        this.versions = List.copyOf(versions);
    }
}
