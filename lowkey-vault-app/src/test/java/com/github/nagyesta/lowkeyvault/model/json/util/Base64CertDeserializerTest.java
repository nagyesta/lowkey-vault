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

import java.util.Base64;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Base64CertDeserializerTest {

    private final Base64CertDeserializer underTest = new Base64CertDeserializer();
    @Mock
    private JsonParser parser;
    @Mock
    private DeserializationContext context;
    private AutoCloseable openMocks;

    public static Stream<Arguments> base64Provider() {
        final var encoder = Base64.getMimeEncoder();
        return Stream.of(EMPTY, BLANK, LOCALHOST)
                .map(String::getBytes)
                .map(b -> Arguments.of(encoder.encodeToString(b), b));
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
    void testDeserializeShouldDecodeBase64WhenCalled(
            final String input,
            final byte[] expected) {
        //given
        when(parser.readValueAs(String.class)).thenReturn(input);

        //when
        final var actual = underTest.deserialize(parser, context);

        //then
        Assertions.assertArrayEquals(expected, actual);
        verify(parser).readValueAs(String.class);
    }
}
