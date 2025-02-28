package com.github.nagyesta.lowkeyvault.template.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.internal.Files;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import jakarta.validation.Validator;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class VaultImporter implements InitializingBean {

    private final VaultImporterProperties vaultImporterProperties;
    private final BackupTemplateProcessor backupTemplateProcessor;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    @Getter
    private final Map<URI, VaultModel> vaults;
    @Getter
    private final Map<URI, List<KeyBackupModel>> keys;
    @Getter
    private final Map<URI, List<SecretBackupModel>> secrets;
    @Getter
    private final Map<URI, List<CertificateBackupModel>> certificates;

    @Autowired
    public VaultImporter(@NonNull final VaultImporterProperties vaultImporterProperties,
                         @NonNull final BackupTemplateProcessor backupTemplateProcessor,
                         @NonNull final ObjectMapper objectMapper,
                         @NonNull final Validator validator) {
        this.vaultImporterProperties = vaultImporterProperties;
        this.backupTemplateProcessor = backupTemplateProcessor;
        this.objectMapper = objectMapper;
        this.validator = validator;
        this.vaults = new TreeMap<>();
        this.keys = new TreeMap<>();
        this.secrets = new TreeMap<>();
        this.certificates = new TreeMap<>();
    }

    public void importTemplates() {
        if (importFileExists()) {
            final var context = vaultImporterProperties.context();
            final var model = readFile(vaultImporterProperties.getImportFile(), context);
            assertValid(model);
            preprocessVaults(model);
        }
    }

    public boolean importFileExists() {
        log.info("Evaluating import file: '{}'", vaultImporterProperties.getImportFile());
        return vaultImporterProperties.importFileExists();
    }

    @Override
    public void afterPropertiesSet() {
        importTemplates();
    }

    void assertValid(final VaultBackupListModel model) {
        final var violations = validator.validate(model);
        if (!violations.isEmpty()) {
            log.error(violations.stream()
                    .map(v -> "'" + v.getPropertyPath() + "': " + v.getMessage()).collect(Collectors.joining(", ")));
            throw new IllegalArgumentException("Import validation failed, please see logs for details!");
        }
    }

    public VaultBackupListModel readFile(final File input, final BackupContext context) {
        try {
            final var jsonTemplate = Files.read(input, StandardCharsets.UTF_8);
            final var json = backupTemplateProcessor.processTemplate(jsonTemplate, context);
            return objectMapper.readValue(json, VaultBackupListModel.class);
        } catch (final IOException ex) {
            throw new IllegalArgumentException("Unable to read file: " + input.getAbsolutePath(), ex);
        }
    }

    private void preprocessVaults(final VaultBackupListModel model) {
        model.getVaults().forEach(v -> {
            preprocessVaultsOfBackup(v);
            preprocessKeysOfBackup(v);
            preprocessSecretsOfBackup(v);
            preprocessCertificatesOfBackup(v);
        });
    }

    private void preprocessVaultsOfBackup(final VaultBackupModel v) {
        vaults.put(v.getAttributes().getBaseUri(), v.getAttributes());
    }

    private void preprocessKeysOfBackup(final VaultBackupModel v) {
        final var keyModelList = keys
                .computeIfAbsent(v.getAttributes().getBaseUri(), k -> new ArrayList<>());
        Optional.ofNullable(v.getKeys()).ifPresent(k -> {
            for (final var value : k.values()) {
                final var backupModel = new KeyBackupModel();
                backupModel.setValue(value);
                keyModelList.add(backupModel);
            }
        });
    }

    private void preprocessSecretsOfBackup(final VaultBackupModel v) {
        final var secretModelList = secrets
                .computeIfAbsent(v.getAttributes().getBaseUri(), k -> new ArrayList<>());
        Optional.ofNullable(v.getSecrets()).ifPresent(s -> {
            for (final var value : s.values()) {
                final var backupModel = new SecretBackupModel();
                backupModel.setValue(value);
                secretModelList.add(backupModel);
            }
        });
    }

    private void preprocessCertificatesOfBackup(final VaultBackupModel v) {
        final var certificateModelList = certificates
                .computeIfAbsent(v.getAttributes().getBaseUri(), k -> new ArrayList<>());
        Optional.ofNullable(v.getCertificates()).ifPresent(c -> {
            for (final var value : c.values()) {
                final var backupModel = new CertificateBackupModel();
                backupModel.setValue(value);
                certificateModelList.add(backupModel);
            }
        });
    }
}
