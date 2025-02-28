package com.github.nagyesta.lowkeyvault.template.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.HTTP_PORT;
import static com.github.nagyesta.lowkeyvault.TestConstants.LOCALHOST;
import static org.mockito.Mockito.mock;

class VaultImporterTest {

    public static Stream<Arguments> nullProvider() {
        final var properties = mock(VaultImporterProperties.class);
        final var processor = mock(BackupTemplateProcessor.class);
        final var mapper = mock(ObjectMapper.class);
        final var validator = mock(Validator.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, processor, mapper, validator))
                .add(Arguments.of(properties, null, mapper, validator))
                .add(Arguments.of(properties, processor, null, validator))
                .add(Arguments.of(properties, processor, mapper, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final VaultImporterProperties vaultImporterProperties, final BackupTemplateProcessor backupTemplateProcessor,
            final ObjectMapper objectMapper, final Validator validator) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new VaultImporter(vaultImporterProperties, backupTemplateProcessor, objectMapper, validator));

        //then + exception
    }

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

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.readFile(properties.getImportFile(), properties.context()));

        //then exception
    }
}
