package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;

import static org.mockito.Mockito.*;

class Base64ZipKeySerializerTest {

    @Test
    void testSerializeShouldThrowExceptionWhenEncodingFails() {
        //given
        final var base64Serializer = mock(Base64Serializer.class);
        final var objectMapper = new ObjectMapper();
        final var underTest = new Base64ZipKeySerializer(base64Serializer, objectMapper);
        final var gen = mock(JsonGenerator.class);
        final var serializers = mock(SerializationContext.class);
        when(base64Serializer.base64Encode(any())).thenThrow(new IllegalStateException("Fail"));
        final var backupList = new KeyBackupList();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.serialize(backupList, gen, serializers));

        //then + exception
        verify(base64Serializer).base64Encode(any());
        verifyNoInteractions(gen, serializers);
    }

    @Test
    void testSerializeShouldWriteNullWhenCalledWithNullInput() {
        //given
        final var underTest = new Base64ZipKeySerializer();
        final var gen = mock(JsonGenerator.class);
        final var serializers = mock(SerializationContext.class);

        //when
        underTest.serialize(null, gen, serializers);

        //then
        verify(gen).writeNull();
        verify(gen, never()).writeString(anyString());
    }
}
