package com.github.nagyesta.lowkeyvault.template.backup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.HTTPS_PORT;
import static com.github.nagyesta.lowkeyvault.TestConstants.LOCALHOST;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VaultImporterPropertiesTest {

    public static Stream<Arguments> fileProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(true, false, false, false, false))
                .add(Arguments.of(false, true, true, true, true))
                .add(Arguments.of(false, false, true, true, false))
                .add(Arguments.of(false, true, false, true, false))
                .add(Arguments.of(false, true, true, false, false))
                .build();
    }

    @Test
    void testContextShouldReturnContextWithThePopulatedDataWhenCalled() {
        //given
        final var host = LOCALHOST;
        final var port = HTTPS_PORT;
        final var underTest = new VaultImporterProperties(null, host, port);

        //when
        final var actual = underTest.context();

        //then
        Assertions.assertEquals(host, actual.getHost());
        Assertions.assertEquals(port, actual.getPort());
    }

    @ParameterizedTest
    @MethodSource("fileProvider")
    void testImportFileExistsShouldReturnFalseWhenTheFileDoesNotExistsCannotBeReadOrIsNotAFile(
            final boolean nullValue,
            final boolean exists,
            final boolean canRead,
            final boolean file,
            final boolean expected) {
        //given
        File input = null;
        if (!nullValue) {
            input = mock(File.class);
            when(input.exists()).thenReturn(exists);
            when(input.canRead()).thenReturn(canRead);
            when(input.isFile()).thenReturn(file);
        }
        final var underTest = new VaultImporterProperties(input, LOCALHOST, HTTPS_PORT);

        //when
        final var actual = underTest.importFileExists();

        //then
        Assertions.assertEquals(expected, actual);
    }
}
