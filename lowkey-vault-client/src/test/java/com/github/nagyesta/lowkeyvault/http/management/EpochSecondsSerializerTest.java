package com.github.nagyesta.lowkeyvault.http.management;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class EpochSecondsSerializerTest {
    private static final int TEN = 10;
    private static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    private static final OffsetDateTime TIME_IN_10_MINUTES = NOW.plusMinutes(TEN).truncatedTo(ChronoUnit.SECONDS);
    private static final OffsetDateTime TIME_10_MINUTES_AGO = NOW.minusMinutes(TEN).truncatedTo(ChronoUnit.SECONDS);

    private final EpochSecondsSerializer underTest = new EpochSecondsSerializer();
    @Mock
    private JsonGenerator generator;
    @Mock
    private SerializerProvider provider;
    @Captor
    private ArgumentCaptor<Long> output;
    private AutoCloseable openMocks;

    public static Stream<Arguments> valueProvider() {
        return Stream.of(null, TIME_10_MINUTES_AGO, NOW, TIME_IN_10_MINUTES)
                .map(v -> Arguments.of(v, Optional.ofNullable(v).map(OffsetDateTime::toEpochSecond).orElse(null)));
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
    void testSerializeShouldConvertOffsetDateTimeWhenCalled(final OffsetDateTime input, final Long expected) throws IOException {
        //given
        doNothing().when(generator).writeNumber(output.capture());

        //when
        underTest.serialize(input, generator, provider);

        //then
        if (expected == null) {
            verify(generator).writeNull();
            verify(generator, never()).writeNumber(anyLong());
        } else {
            verify(generator).writeNumber(anyLong());
            verify(generator, never()).writeNull();
            final var actual = output.getAllValues();
            Assertions.assertEquals(1, actual.size());
            Assertions.assertEquals(expected, actual.get(0));
        }
    }
}
