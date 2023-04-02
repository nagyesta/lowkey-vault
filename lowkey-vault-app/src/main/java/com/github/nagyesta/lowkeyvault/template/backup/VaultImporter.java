package com.github.nagyesta.lowkeyvault.template.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.jknack.handlebars.internal.Files;
import com.github.nagyesta.lowkeyvault.model.common.backup.*;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
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
    private final Map<URI, VaultModel> vaults;
    private final Map<URI, List<KeyBackupModel>> keys;
    private final Map<URI, List<SecretBackupModel>> secrets;
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
            final BackupContext context = vaultImporterProperties.context();
            final VaultBackupListModel model = readFile(vaultImporterProperties.getImportFile(), context);
            assertValid(model);
            preprocessVaults(model);
        }
    }

    public boolean importFileExists() {
        log.info("Evaluating import file: '{}'", vaultImporterProperties.getImportFile());
        return vaultImporterProperties.importFileExists();
    }

    public Map<URI, VaultModel> getVaults() {
        return vaults;
    }

    public Map<URI, List<KeyBackupModel>> getKeys() {
        return keys;
    }

    public Map<URI, List<SecretBackupModel>> getSecrets() {
        return secrets;
    }

    public Map<URI, List<CertificateBackupModel>> getCertificates() {
        return certificates;
    }

    @Override
    public void afterPropertiesSet() {
        importTemplates();
    }

    void assertValid(final VaultBackupListModel model) {
        final Set<ConstraintViolation<VaultBackupListModel>> violations = validator.validate(model);
        if (!violations.isEmpty()) {
            log.error(violations.stream()
                    .map(v -> "'" + v.getPropertyPath() + "': " + v.getMessage()).collect(Collectors.joining(", ")));
            throw new IllegalArgumentException("Import validation failed, please see logs for details!");
        }
    }

    public VaultBackupListModel readFile(final File input, final BackupContext context) {
        try {
            final String jsonTemplate = Files.read(input, StandardCharsets.UTF_8);
            final String json = backupTemplateProcessor.processTemplate(jsonTemplate, context);
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
        final List<KeyBackupModel> keyModelList = keys
                .computeIfAbsent(v.getAttributes().getBaseUri(), k -> new ArrayList<>());
        Optional.ofNullable(v.getKeys()).ifPresent(k -> {
            for (final KeyBackupList value : k.values()) {
                final KeyBackupModel backupModel = new KeyBackupModel();
                backupModel.setValue(value);
                keyModelList.add(backupModel);
            }
        });
    }

    private void preprocessSecretsOfBackup(final VaultBackupModel v) {
        final List<SecretBackupModel> secretModelList = secrets
                .computeIfAbsent(v.getAttributes().getBaseUri(), k -> new ArrayList<>());
        Optional.ofNullable(v.getSecrets()).ifPresent(s -> {
            for (final SecretBackupList value : s.values()) {
                final SecretBackupModel backupModel = new SecretBackupModel();
                backupModel.setValue(value);
                secretModelList.add(backupModel);
            }
        });
    }

    private void preprocessCertificatesOfBackup(final VaultBackupModel v) {
        final List<CertificateBackupModel> certificateModelList = certificates
                .computeIfAbsent(v.getAttributes().getBaseUri(), k -> new ArrayList<>());
        Optional.ofNullable(v.getCertificates()).ifPresent(c -> {
            for (final CertificateBackupList value : c.values()) {
                final CertificateBackupModel backupModel = new CertificateBackupModel();
                backupModel.setValue(value);
                certificateModelList.add(backupModel);
            }
        });
    }
}
