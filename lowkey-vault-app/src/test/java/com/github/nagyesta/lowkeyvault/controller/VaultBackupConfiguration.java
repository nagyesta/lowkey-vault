package com.github.nagyesta.lowkeyvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.context.CertificateConverterConfiguration;
import com.github.nagyesta.lowkeyvault.context.KeyConverterConfiguration;
import com.github.nagyesta.lowkeyvault.context.SecretConverterConfiguration;
import com.github.nagyesta.lowkeyvault.controller.v7_3.CertificateBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.SecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.mapper.common.VaultFakeToVaultModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
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

import java.io.File;
import java.util.Objects;

import static org.mockito.Mockito.mock;

@Profile("vault")
@Configuration
@Import({BaseVaultConfiguration.class, CertificateConverterConfiguration.class,
        KeyConverterConfiguration.class, SecretConverterConfiguration.class})
public class VaultBackupConfiguration {

    @Autowired
    private VaultService vaultService;
    @Autowired
    private CertificateConverterRegistry certificateConverterRegistry;
    @Autowired
    private SecretConverterRegistry secretConverterRegistry;
    @Autowired
    private KeyConverterRegistry keyConverterRegistry;

    @Bean
    public SecretBackupRestoreController secretBackupRestoreController() {
        return new SecretBackupRestoreController(secretConverterRegistry, vaultService);
    }

    @Bean
    public KeyBackupRestoreController keyBackupRestoreController() {
        return new KeyBackupRestoreController(keyConverterRegistry, vaultService);
    }

    @Bean
    public CertificateBackupRestoreController certificateBackupRestoreController() {
        return new CertificateBackupRestoreController(certificateConverterRegistry, vaultService);
    }

    @Bean
    public VaultManagementController vaultManagementController() {
        return new VaultManagementController(vaultService, vaultFakeToVaultModelConverter());
    }

    @Bean
    public VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter() {
        return new VaultFakeToVaultModelConverter();
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @Bean
    public VaultImporterProperties vaultImporterProperties() {
        final String file = Objects.requireNonNull(getClass().getResource("/template/full-import.json.hbs")).getFile();
        return new VaultImporterProperties(new File(file), "127.0.0.1", 8444);
    }

    @Bean
    public VaultImporter vaultImporter() {
        return new VaultImporter(vaultImporterProperties(), backupTemplateProcessor(), objectMapper(), validator());
    }

    @Bean
    public VaultImportExportExecutor vaultImportExportExecutor() {
        return new VaultImportExportExecutor(vaultManagementController(), keyBackupRestoreController(),
                secretBackupRestoreController(), certificateBackupRestoreController());
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
        return new ObjectMapper().findAndRegisterModules();
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
