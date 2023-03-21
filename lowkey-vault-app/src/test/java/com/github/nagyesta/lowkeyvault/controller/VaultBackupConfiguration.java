package com.github.nagyesta.lowkeyvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.SecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.common.VaultFakeToVaultModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72PropertiesModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.validator.ImportKeyValidator;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultServiceImpl;
import com.github.nagyesta.lowkeyvault.template.backup.BackupTemplateProcessor;
import com.github.nagyesta.lowkeyvault.template.backup.TimeHelperSource;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.validation.Validator;
import java.io.File;
import java.util.Objects;

import static org.mockito.Mockito.mock;

@Profile("vault")
@Configuration
public class VaultBackupConfiguration {

    @Bean
    public SecretBackupRestoreController secretBackupRestoreController() {
        return new SecretBackupRestoreController(secretConverterRegistry(), vaultService());
    }

    @Bean
    public SecretEntityToV72ModelConverter secretEntityToV72ModelConverter() {
        return new SecretEntityToV72ModelConverter(secretConverterRegistry());
    }

    @Bean
    public SecretEntityToV72BackupConverter secretEntityToV72BackupConverter() {
        return new SecretEntityToV72BackupConverter(secretConverterRegistry());
    }

    @Bean
    public SecretEntityToV72PropertiesModelConverter secretEntityToV72PropertiesModelConverter() {
        return new SecretEntityToV72PropertiesModelConverter(secretConverterRegistry());
    }

    @Bean
    public SecretConverterRegistry secretConverterRegistry() {
        return new SecretConverterRegistry();
    }

    @Bean
    public KeyConverterRegistry keyConverterRegistry() {
        return new KeyConverterRegistry();
    }

    @Bean
    public KeyBackupRestoreController keyBackupRestoreController() {
        return new KeyBackupRestoreController(keyConverterRegistry(), vaultService());
    }

    @Bean
    public KeyEntityToV72ModelConverter keyEntityToV72ModelConverter() {
        return new KeyEntityToV72ModelConverter(keyConverterRegistry());
    }

    @Bean
    public KeyEntityToV72PropertiesModelConverter keyEntityToV72PropertiesModelConverter() {
        return new KeyEntityToV72PropertiesModelConverter(keyConverterRegistry());
    }

    @Bean
    public KeyEntityToV72BackupConverter keyEntityToV72BackupConverter() {
        return new KeyEntityToV72BackupConverter(keyConverterRegistry());
    }

    @Bean
    public KeyRotationPolicyToV73ModelConverter keyRotationPolicyToV73ModelConverter() {
        return new KeyRotationPolicyToV73ModelConverter(keyConverterRegistry());
    }

    @Bean
    public KeyRotationPolicyV73ModelToEntityConverter keyRotationPolicyV73ModelToEntityConverter() {
        return new KeyRotationPolicyV73ModelToEntityConverter(keyConverterRegistry());
    }

    @Bean
    public VaultManagementController vaultManagementController() {
        return new VaultManagementController(vaultService(), vaultFakeToVaultModelConverter());
    }

    @Bean
    public VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter() {
        return new VaultFakeToVaultModelConverter();
    }

    @Bean
    public VaultService vaultService() {
        return new VaultServiceImpl();
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
