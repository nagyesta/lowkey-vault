package com.github.nagyesta.lowkeyvault.model.v7_2.secret;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@EqualsAndHashCode
public class SecretBackupList implements BackupListContainer<SecretBackupListItem> {

    @Valid
    @NotNull
    @Size(min = 1)
    @JsonProperty("versions")
    private List<SecretBackupListItem> versions = List.of();

    public List<SecretBackupListItem> getVersions() {
        return versions;
    }

    public void setVersions(final List<SecretBackupListItem> versions) {
        this.versions = List.copyOf(versions);
    }
}
