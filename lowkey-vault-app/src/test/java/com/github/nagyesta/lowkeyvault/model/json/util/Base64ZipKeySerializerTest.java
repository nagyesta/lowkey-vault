package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class Base64ZipKeySerializerTest {

    @Test
    void testSerializeShouldThrowExceptionWhenEncodingFails() {
        //given
        final Base64Serializer base64Serializer = mock(Base64Serializer.class);
        final ObjectMapper objectMapper = new ObjectMapper();
        final Base64ZipKeySerializer underTest = new Base64ZipKeySerializer(base64Serializer, objectMapper);
        final JsonGenerator gen = mock(JsonGenerator.class);
        final SerializerProvider serializers = mock(SerializerProvider.class);
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
        final Base64ZipKeySerializer underTest = new Base64ZipKeySerializer();
        final JsonGenerator gen = mock(JsonGenerator.class);
        final SerializerProvider serializers = mock(SerializerProvider.class);

        //when
        underTest.serialize(null, gen, serializers);

        //then
        verify(gen).writeNull();
        verify(gen, never()).writeString(anyString());
    }
}
