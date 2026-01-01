package com.github.nagyesta.lowkeyvault.template.backup;

import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

import static com.github.nagyesta.lowkeyvault.TestConstants.HTTP_PORT;
import static com.github.nagyesta.lowkeyvault.TestConstants.LOCALHOST;

class VaultImporterTest {

    @Test
    void testAssertValidShouldThrowExceptionWhenValueIsInvalid() {
        //given
        //noinspection resource
        final var factory = Validation.buildDefaultValidatorFactory();
        final var validator = factory.getValidator();
        final var underTest = new VaultImporter(new VaultImporterProperties(null, LOCALHOST, HTTP_PORT),
                new BackupTemplateProcessor(new TimeHelperSource()),
                new ObjectMapper(),
                validator);
        final var input = new VaultBackupListModel();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.assertValid(input));

        //then exception
    }

    @Test
    void testReadFileShouldThrowExceptionWhenFileIsNotFound() {
        //given
        //noinspection resource
        final var factory = Validation.buildDefaultValidatorFactory();
        final var validator = factory.getValidator();
        final var properties = new VaultImporterProperties(new File("not-found"), LOCALHOST, HTTP_PORT);
        final var processor = new BackupTemplateProcessor(new TimeHelperSource());
        final var underTest = new VaultImporter(properties, processor, new ObjectMapper(), validator);
        final var importFile = properties.getImportFile();
        final var context = properties.context();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.readFile(importFile, context));

        //then exception
    }
}
