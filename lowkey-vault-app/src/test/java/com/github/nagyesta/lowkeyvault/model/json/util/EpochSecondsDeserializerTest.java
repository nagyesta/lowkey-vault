package com.github.nagyesta.lowkeyvault.model.json.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EpochSecondsDeserializerTest {

    private final EpochSecondsDeserializer underTest = new EpochSecondsDeserializer();
    @Mock
    private JsonParser parser;
    @Mock
    private DeserializationContext context;
    private AutoCloseable openMocks;

    public static Stream<Arguments> valueProvider() {
        return Stream.of(null, TIME_10_MINUTES_AGO, NOW, TIME_IN_10_MINUTES)
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
    void testDeserializeShouldCreateOffsetDateTimeWhenCalled(final Long input, final OffsetDateTime expected) {
        //given
        when(parser.readValueAs(Long.class)).thenReturn(input);

        //when
        final var actual = underTest.deserialize(parser, context);

        //then
        Assertions.assertEquals(expected, actual);
        verify(parser).readValueAs(Long.class);
    }
}
