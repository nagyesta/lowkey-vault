package com.github.nagyesta.lowkeyvault.model.json.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

class CertificateLifetimeActionSerializationIntegrationTest {

    public static final String VALID_FULL_AUTO_RENEW = "{\"action\":{\"action_type\":\"AutoRenew\"}}";
    public static final String VALID_NULL_ACTION_TYPE = "{\"action\":null}";
    public static final String INVALID_NULL_ACTION_TYPE = "{\"action\":{\"action_type\":null}}";
    public static final String INVALID_UNKNOWN_ACTION_TYPE = "{\"action\":{\"action_type\":\"unknown\"}}";

    @Test
    void testSerializeShouldGenerateObjectRepresentationWhenCalledWithNonNullValue() throws JsonProcessingException {
        //given
        final var objectMapper = new ObjectMapper();
        final var objectWriter = objectMapper.writer();
        final var input = new TestObjectType();
        input.setAction(CertificateLifetimeActionActivity.AUTO_RENEW);

        //when
        final var actual = objectWriter.writeValueAsString(input);

        //then
        Assertions.assertEquals(VALID_FULL_AUTO_RENEW, actual);
    }

    @Test
    void testDeserializeShouldReturnExpectedEnumWhenCalledWithValidJsonObject() throws IOException {
        //given
        final var objectMapper = new ObjectMapper();
        final var objectReader = objectMapper.readerFor(TestObjectType.class);

        //when
        final TestObjectType actual = objectReader.readValue(VALID_FULL_AUTO_RENEW);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(CertificateLifetimeActionActivity.AUTO_RENEW, actual.getAction());
    }

    @Test
    void testDeserializeShouldReturnExpectedEnumWhenCalledWithNullJsonValue() throws IOException {
        //given
        final var objectMapper = new ObjectMapper();
        final var objectReader = objectMapper.readerFor(TestObjectType.class);

        //when
        final TestObjectType actual = objectReader.readValue(VALID_NULL_ACTION_TYPE);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertNull(actual.getAction());
    }

    @ParameterizedTest
    @ValueSource(strings = {INVALID_NULL_ACTION_TYPE, INVALID_UNKNOWN_ACTION_TYPE})
    void testDeserializeShouldReturnExpectedEnumWhenCalledWithInvalidJsonObject(final String json) {
        //given
        final var objectMapper = new ObjectMapper();
        final var objectReader = objectMapper.readerFor(TestObjectType.class);

        //when
        Assertions.assertThrows(JsonMappingException.class, () -> objectReader.readValue(json));

        //then + exception
    }

    @Data
    private static final class TestObjectType {
        @JsonSerialize(using = CertificateLifetimeActionSerializer.class)
        @JsonDeserialize(using = CertificateLifetimeActionDeserializer.class)
        private CertificateLifetimeActionActivity action;
    }
}
