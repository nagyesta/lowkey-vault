package com.github.nagyesta.lowkeyvault.template.backup;

import com.github.nagyesta.lowkeyvault.ResourceUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.stream.Stream;

class BackupTemplateProcessorIntegrationTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("/template/no-placeholder.json",
                        "", 0, 0, "/template/no-placeholder.json"))
                .add(Arguments.of("/template/host-placeholder-input.json.hbs",
                        "localhost", 8443, 10000, "/template/no-placeholder.json"))
                .add(Arguments.of("/template/port-placeholder-input.json.hbs",
                        "localhost", 8443, 10000, "/template/no-placeholder.json"))
                .add(Arguments.of("/template/time-placeholder-input.json.hbs",
                        "localhost", 8443, 9000, "/template/no-placeholder.json"))
                .add(Arguments.of("/template/all-placeholder-input.json.hbs",
                        "localhost", 8443, 10000, "/template/no-placeholder.json"))
                .build();
    }

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(null, new BackupContext("localhost", 0)))
                .add(Arguments.of("template", null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validProvider")
    void testProcessTemplateShouldFillPlaceholdersWhenCalledWithValidData(
            final String templateResource, final String host, final int port, final int timeEpochSeconds, final String expectedResource)
            throws IOException {
        //given
        final OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(timeEpochSeconds), ZoneOffset.UTC);
        final BackupContext context = new BackupContext(host, port);
        final TimeHelperSource timeHelperSource = new TimeHelperSource(time);
        final BackupTemplateProcessor underTest = new BackupTemplateProcessor(timeHelperSource);
        final String templateAsString = Objects.requireNonNull(ResourceUtils.loadResourceAsString(templateResource));
        final String expectedAsString = ResourceUtils.loadResourceAsString(expectedResource);

        //when
        final String actual = underTest.processTemplate(templateAsString, context);

        //then
        Assertions.assertEquals(expectedAsString, actual);
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testProcessTemplateShouldThrowExceptionWhenCalledWithNull(final String template, final BackupContext context) {
        //given
        final BackupTemplateProcessor underTest = new BackupTemplateProcessor(new TimeHelperSource());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.processTemplate(template, context));

        //then + exception
    }
}
