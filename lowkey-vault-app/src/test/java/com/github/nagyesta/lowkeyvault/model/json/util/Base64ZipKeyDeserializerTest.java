package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;

class Base64ZipKeyDeserializerTest {

    @Test
    void testDeserializeShouldThrowExceptionWhenDecodingFails() {
        //given
        final var base64Deserializer = mock(Base64Deserializer.class);
        final var objectMapper = mock(ObjectMapper.class);
        final var underTest = new Base64ZipKeyDeserializer(base64Deserializer, objectMapper);
        final var jsonParser = mock(JsonParser.class);
        final var context = mock(DeserializationContext.class);
        when(jsonParser.readValueAs(String.class)).thenReturn("");
        when(base64Deserializer.deserializeBase64("")).thenReturn(new byte[1]);
        when(objectMapper.reader()).thenThrow(new IllegalStateException("Fail"));

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.deserialize(jsonParser, context));

        //then + exception
        verify(jsonParser).readValueAs(String.class);
        verify(base64Deserializer).deserializeBase64("");
    }

    @Test
    void testDeserializeShouldWriteNullWhenCalledWithNullInput() {
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
