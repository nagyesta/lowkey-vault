package com.github.nagyesta.lowkeyvault.model.json.util;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

class CertificateLifetimeActionSerializationIntegrationTest {

    private static final String VALID_FULL_AUTO_RENEW = "{\"action\":{\"action_type\":\"AutoRenew\"}}";
    private static final String VALID_NULL_ACTION_TYPE = "{\"action\":null}";
    private static final String INVALID_NULL_ACTION_TYPE = "{\"action\":{\"action_type\":null}}";
    private static final String INVALID_UNKNOWN_ACTION_TYPE = "{\"action\":{\"action_type\":\"unknown\"}}";

    @Test
    void testSerializeShouldGenerateObjectRepresentationWhenCalledWithNonNullValue() throws JacksonException {
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
    void testDeserializeShouldReturnExpectedEnumWhenCalledWithValidJsonObject() {
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
    void testDeserializeShouldReturnExpectedEnumWhenCalledWithNullJsonValue() {
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
        Assertions.assertThrows(DatabindException.class, () -> objectReader.readValue(json));

        //then + exception
    }

    @Data
    private static final class TestObjectType {
        @JsonSerialize(using = CertificateLifetimeActionSerializer.class)
        @JsonDeserialize(using = CertificateLifetimeActionDeserializer.class)
        private CertificateLifetimeActionActivity action;
    }
}
