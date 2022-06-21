package com.github.nagyesta.lowkeyvault.template.backup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.File;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.HTTP_PORT;
import static com.github.nagyesta.lowkeyvault.TestConstants.LOCALHOST;
import static org.mockito.Mockito.mock;

class VaultImporterTest {

    public static Stream<Arguments> nullProvider() {
        final VaultImporterProperties properties = mock(VaultImporterProperties.class);
        final BackupTemplateProcessor processor = mock(BackupTemplateProcessor.class);
        final ObjectMapper mapper = mock(ObjectMapper.class);
        final Validator validator = mock(Validator.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(properties, null, null, null))
                .add(Arguments.of(null, processor, null, null))
                .add(Arguments.of(null, null, mapper, null))
                .add(Arguments.of(null, null, null, validator))
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
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final VaultImporter underTest = new VaultImporter(new VaultImporterProperties(null, LOCALHOST, HTTP_PORT),
                new BackupTemplateProcessor(new TimeHelperSource()),
                new ObjectMapper(),
                validator);
        final VaultBackupListModel input = new VaultBackupListModel();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.assertValid(input));

        //then exception
    }

    @Test
    void testReadFileShouldThrowExceptionWhenFileIsNotFound() {
        //given
        //noinspection resource
        final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();
        final VaultImporterProperties properties = new VaultImporterProperties(new File("not-found"), LOCALHOST, HTTP_PORT);
        final BackupTemplateProcessor processor = new BackupTemplateProcessor(new TimeHelperSource());
        final VaultImporter underTest = new VaultImporter(properties, processor, new ObjectMapper(), validator);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.readFile(properties.getImportFile(), properties.context()));

        //then exception
    }
}
