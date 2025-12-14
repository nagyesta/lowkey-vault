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

    @ParameterizedTest
    @MethodSource("validProvider")
    void testProcessTemplateShouldFillPlaceholdersWhenCalledWithValidData(
            final String templateResource,
            final String host,
            final int port,
            final int timeEpochSeconds,
            final String expectedResource) throws IOException {
        //given
        final var time = OffsetDateTime.ofInstant(Instant.ofEpochSecond(timeEpochSeconds), ZoneOffset.UTC);
        final var context = new BackupContext(host, port);
        final var timeHelperSource = new TimeHelperSource(time);
        final var underTest = new BackupTemplateProcessor(timeHelperSource);
        final var templateAsString = Objects.requireNonNull(ResourceUtils.loadResourceAsString(templateResource));
        final var expectedAsString = ResourceUtils.loadResourceAsString(expectedResource);

        //when
        final var actual = underTest.processTemplate(templateAsString, context);

        //then
        Assertions.assertEquals(expectedAsString, actual);
    }
}
