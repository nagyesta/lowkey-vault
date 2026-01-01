package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EpochSecondsDeserializerTest {
    private static final int TEN = 10;
    private static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    private static final OffsetDateTime TIME_IN_10_MINUTES = NOW.plusMinutes(TEN).truncatedTo(ChronoUnit.SECONDS);
    private static final OffsetDateTime TIME_10_MINUTES_AGO = NOW.minusMinutes(TEN).truncatedTo(ChronoUnit.SECONDS);

    private final EpochSecondsDeserializer underTest = new EpochSecondsDeserializer();
    @Mock
    private JsonParser parser;
    @Mock
    private DeserializationContext context;
    private AutoCloseable openMocks;

    public static Stream<Arguments> valueProvider() {
        return Stream.of(TIME_10_MINUTES_AGO, NOW, TIME_IN_10_MINUTES)
                .map(v -> Arguments.of(Optional.ofNullable(v).map(OffsetDateTime::toEpochSecond).orElse(null), v));
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testDeserializeShouldCreateOffsetDateTimeWhenCalled(
            final Long input,
            final OffsetDateTime expected) throws IOException {
        //given
        when(parser.readValueAs(Long.class)).thenReturn(input);

        //when
        final var actual = underTest.deserialize(parser, context);

        //then
        Assertions.assertEquals(expected, actual);
        verify(parser).readValueAs(Long.class);
    }
}
