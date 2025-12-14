package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.controller.v7_2.SecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.CertificateBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.mapper.common.VaultFakeToVaultModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateLifetimeActionsPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.validator.ImportKeyValidator;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.template.backup.BackupTemplateProcessor;
import com.github.nagyesta.lowkeyvault.template.backup.TimeHelperSource;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporterProperties;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Objects;

import static org.mockito.Mockito.mock;

@Profile("vault")
@Configuration
@Import({BaseVaultConfiguration.class})
public class VaultBackupConfiguration {

    @Autowired
    private VaultService vaultService;

    @Bean
    public SecretBackupRestoreController secretBackupRestoreController(
            final SecretEntityToV72ModelConverter modelConverter,
            final SecretEntityToV72SecretItemModelConverter itemConverter,
            final SecretEntityToV72BackupConverter backupConverter) {
        return new SecretBackupRestoreController(vaultService, modelConverter, itemConverter, backupConverter);
    }

    @Bean
    public KeyBackupRestoreController keyBackupRestoreController(
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72KeyItemModelConverter itemConverter,
            final KeyEntityToV72BackupConverter backupConverter,
            final KeyRotationPolicyToV73ModelConverter rotationPolicyModelConverter,
            final KeyRotationPolicyV73ModelToEntityConverter rotationPolicyEntityConverter) {
        return new KeyBackupRestoreController(
                vaultService, modelConverter, itemConverter, backupConverter, rotationPolicyModelConverter, rotationPolicyEntityConverter);
    }

    @Bean
    public CertificateBackupRestoreController certificateBackupRestoreController(
            final CertificateEntityToV73ModelConverter modelConverter,
            final CertificateEntityToV73CertificateItemModelConverter itemConverter,
            final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter,
            final CertificateEntityToV73BackupConverter backupConverter) {
        return new CertificateBackupRestoreController(
                vaultService, modelConverter, itemConverter, lifetimeActionConverter, backupConverter);
    }

    @Bean
    public VaultManagementController vaultManagementController(
            final VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter) {
        return new VaultManagementController(vaultService, vaultFakeToVaultModelConverter);
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @Bean
    public VaultImporterProperties vaultImporterProperties() {
        final var file = Objects.requireNonNull(getClass().getResource("/template/full-import.json.hbs")).getFile();
        return new VaultImporterProperties(new File(file), "127.0.0.1", 8444);
    }

    @Bean
    public VaultImporter vaultImporter() {
        return new VaultImporter(vaultImporterProperties(), backupTemplateProcessor(), objectMapper(), validator());
    }

    @Bean
    public VaultImportExportExecutor vaultImportExportExecutor(
            final VaultManagementController vaultManagementController,
            final KeyBackupRestoreController keyBackupRestoreController,
            final SecretBackupRestoreController secretBackupRestoreController,
            final CertificateBackupRestoreController certificateBackupRestoreController) {
        return new VaultImportExportExecutor(vaultManagementController, keyBackupRestoreController,
                secretBackupRestoreController, certificateBackupRestoreController);
    }

    @Bean
    public BackupTemplateProcessor backupTemplateProcessor() {
        return new BackupTemplateProcessor(timeHelperSource());
    }

    @Bean
    public TimeHelperSource timeHelperSource() {
        return new TimeHelperSource();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ImportKeyValidator importKeyValidator() {
        return new ImportKeyValidator(validator());
    }

    @Bean
    public Validator validator() {
        return mock(Validator.class);
    }
}
