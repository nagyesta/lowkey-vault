package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode
public class CertificateBackupList implements BackupListContainer<CertificateBackupListItem> {

    @Valid
    @NotNull
    @Size(min = 1)
    @JsonProperty("versions")
    private List<CertificateBackupListItem> versions = List.of();

    @Override
    public List<CertificateBackupListItem> getVersions() {
        return versions;
    }

    @Override
    public void setVersions(final List<CertificateBackupListItem> versions) {
        this.versions = List.copyOf(versions);
    }

}
