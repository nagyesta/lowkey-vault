package com.github.nagyesta.lowkeyvault.template.backup;

import lombok.Data;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.File;
import java.util.Objects;

@Data
@Component
public class VaultImporterProperties {

    @Nullable
    private final File importFile;
    private final String importTemplateHost;
    private final int importTemplatePort;

    public VaultImporterProperties(
            @Nullable @Value("${LOWKEY_IMPORT_LOCATION}") final File importFile,
            @Value("${LOWKEY_IMPORT_TEMPLATE_HOST:localhost}") final String importTemplateHost,
            @Value("${LOWKEY_IMPORT_TEMPLATE_PORT:${server.port}}") final int importTemplatePort) {
        this.importFile = importFile;
        this.importTemplateHost = importTemplateHost;
        this.importTemplatePort = importTemplatePort;
    }

    public BackupContext context() {
        return new BackupContext(importTemplateHost, importTemplatePort);
    }

    public boolean importFileExists() {
        return importFile != null && importFile.exists() && importFile.isFile() && importFile.canRead();
    }

    public File getExistingImportFile() {
        Assert.isTrue(importFileExists(), "Import file must exist and be readable.");
        return Objects.requireNonNull(importFile);
    }
}
