package com.github.nagyesta.lowkeyvault.model.json.util;

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
import java.util.Base64;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static org.mockito.Mockito.*;

class Base64SerializerTest {

    private final Base64Serializer underTest = new Base64Serializer();
    @Mock
    private JsonGenerator generator;
    @Mock
    private SerializerProvider provider;
    @Captor
    private ArgumentCaptor<String> output;
    private AutoCloseable openMocks;

    public static Stream<Arguments> base64Provider() {
        final var encoder = Base64.getUrlEncoder().withoutPadding();
        return Stream.of(null, EMPTY, BLANK, LOCALHOST)
                .map(s -> Optional.ofNullable(s).map(String::getBytes).orElse(null))
                .map(b -> Arguments.of(b, Optional.ofNullable(b).filter(v -> v.length > 0).map(encoder::encodeToString).orElse(null)));
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
    void testSerializeShouldEncodeBase64WhenCalled(final byte[] input, final String expected) throws IOException {
        //given
        doNothing().when(generator).writeString(output.capture());

        //when
        underTest.serialize(input, generator, provider);

        //then
        if (expected == null) {
            verify(generator).writeNull();
            verify(generator, never()).writeString(anyString());
        } else {
            verify(generator).writeString(anyString());
            verify(generator, never()).writeNull();
            final var actual = output.getAllValues();
            Assertions.assertEquals(1, actual.size());
            Assertions.assertEquals(expected, actual.get(0));
        }
    }
}
