package com.github.nagyesta.lowkeyvault.model.json.util;

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
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Base64MimeDeserializerTest {

    private final Base64MimeDeserializer underTest = new Base64MimeDeserializer();
    @Mock
    private JsonParser parser;
    @Mock
    private DeserializationContext context;
    private AutoCloseable openMocks;

    public static Stream<Arguments> base64Provider() {
        final Base64.Encoder encoder = Base64.getMimeEncoder();
        return Stream.of(null, EMPTY, BLANK, LOCALHOST)
                .map(s -> Optional.ofNullable(s).map(String::getBytes).orElse(null))
                .map(b -> Arguments.of(Optional.ofNullable(b).map(encoder::encodeToString).orElse(null), b));
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
    @MethodSource("base64Provider")
    void testDeserializeShouldDecodeBase64WhenCalled(final String input, final byte[] expected) throws IOException {
        //given
        when(parser.readValueAs(eq(String.class))).thenReturn(input);

        //when
        final byte[] actual = underTest.deserialize(parser, context);

        //then
        Assertions.assertArrayEquals(expected, actual);
        verify(parser).readValueAs(eq(String.class));
    }
}
