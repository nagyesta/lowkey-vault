package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.Mockito.*;

class Base64ZipKeyDeserializerTest {

    @Test
    void testDeserializeShouldThrowExceptionWhenDecodingFails() throws IOException {
        //given
        final var base64Deserializer = mock(Base64Deserializer.class);
        final var objectMapper = mock(ObjectMapper.class);
        final var underTest = new Base64ZipKeyDeserializer(base64Deserializer, objectMapper);
        final var jsonParser = mock(JsonParser.class);
        final var context = mock(DeserializationContext.class);
        when(base64Deserializer.deserializeBase64(jsonParser)).thenReturn(new byte[1]);
        when(objectMapper.reader()).thenThrow(new IllegalStateException("Fail"));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.deserialize(jsonParser, context));

        //then + exception
        verify(base64Deserializer).deserializeBase64(jsonParser);
    }

    @Test
    void testDeserializeShouldWriteNullWhenCalledWithNullInput() throws IOException {
        //given
        final var underTest = new Base64ZipKeyDeserializer();
        final var jsonParser = mock(JsonParser.class);
        when(jsonParser.readValueAs(String.class)).thenReturn("");
        final var context = mock(DeserializationContext.class);

        //when
        final var actual = underTest.deserialize(jsonParser, context);

        //then
        Assertions.assertNull(actual);
        verify(jsonParser).readValueAs(String.class);
    }

    @Test
    void testGetTypeShouldReturnCorrectTypeWhenCalled() {
        //given
        final var underTest = new Base64ZipKeyDeserializer();

        //when
        final var actual = underTest.getType();

        //then
        Assertions.assertEquals(KeyBackupList.class, actual);
    }
}
