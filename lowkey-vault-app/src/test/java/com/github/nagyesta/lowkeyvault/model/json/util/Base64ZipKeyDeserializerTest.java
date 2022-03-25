package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class Base64ZipKeyDeserializerTest {

    @Test
    void testDeserializeShouldThrowExceptionWhenDecodingFails() throws IOException {
        //given
        final Base64Deserializer base64Deserializer = mock(Base64Deserializer.class);
        final ObjectMapper objectMapper = mock(ObjectMapper.class);
        final Base64ZipKeyDeserializer underTest = new Base64ZipKeyDeserializer(base64Deserializer, objectMapper);
        final JsonParser jsonParser = mock(JsonParser.class);
        final DeserializationContext context = mock(DeserializationContext.class);
        when(base64Deserializer.deserializeBase64(eq(jsonParser))).thenReturn(new byte[1]);
        when(objectMapper.reader()).thenThrow(new IllegalStateException("Fail"));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.deserialize(jsonParser, context));

        //then + exception
        verify(base64Deserializer).deserializeBase64(eq(jsonParser));
    }

    @Test
    void testDeserializeShouldWriteNullWhenCalledWithNullInput() throws IOException {
        //given
        final Base64ZipKeyDeserializer underTest = new Base64ZipKeyDeserializer();
        final JsonParser jsonParser = mock(JsonParser.class);
        when(jsonParser.readValueAs(eq(String.class))).thenReturn("");
        final DeserializationContext context = mock(DeserializationContext.class);

        //when
        final KeyBackupList actual = underTest.deserialize(jsonParser, context);

        //then
        Assertions.assertNull(actual);
        verify(jsonParser).readValueAs(eq(String.class));
    }
}
