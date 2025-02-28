package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class Base64ZipKeySerializerTest {

    @Test
    void testSerializeShouldThrowExceptionWhenEncodingFails() {
        //given
        final var base64Serializer = mock(Base64Serializer.class);
        final var objectMapper = new ObjectMapper();
        final var underTest = new Base64ZipKeySerializer(base64Serializer, objectMapper);
        final var gen = mock(JsonGenerator.class);
        final var serializers = mock(SerializerProvider.class);
        when(base64Serializer.base64Encode(any())).thenThrow(new IllegalStateException("Fail"));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.serialize(new KeyBackupList(), gen, serializers));

        //then + exception
        verify(base64Serializer).base64Encode(any());
        verifyNoInteractions(gen, serializers);
    }

    @Test
    void testSerializeShouldWriteNullWhenCalledWithNullInput() throws IOException {
        //given
        final var underTest = new Base64ZipKeySerializer();
        final var gen = mock(JsonGenerator.class);
        final var serializers = mock(SerializerProvider.class);

        //when
        underTest.serialize(null, gen, serializers);

        //then
        verify(gen).writeNull();
        verify(gen, never()).writeString(anyString());
    }
}
