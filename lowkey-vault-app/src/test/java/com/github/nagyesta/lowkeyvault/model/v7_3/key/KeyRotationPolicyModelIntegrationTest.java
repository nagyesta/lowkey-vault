package com.github.nagyesta.lowkeyvault.model.v7_3.key;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyLifetimeActionTrigger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@SpringBootTest
@TestPropertySource(locations = "classpath:test-application.properties")
class KeyRotationPolicyModelIntegrationTest {

    private static final KeyLifetimeActionTrigger TRIGGER_90_DAYS_AFTER_CREATION =
            new KeyLifetimeActionTrigger(Period.ofDays(90), LifetimeActionTriggerType.TIME_AFTER_CREATE);
    private static final KeyLifetimeActionModel ROTATE_ACTION =
            new KeyLifetimeActionModel(
                    new KeyLifetimeActionTypeModel(LifetimeActionType.ROTATE),
                    new KeyLifetimeActionTriggerModel(TRIGGER_90_DAYS_AFTER_CREATION));
    private static final KeyLifetimeActionTrigger TRIGGER_30_DAYS_BEFORE_EXPIRY =
            new KeyLifetimeActionTrigger(Period.ofDays(30), LifetimeActionTriggerType.TIME_BEFORE_EXPIRY);
    private static final KeyLifetimeActionModel NOTIFY_ACTION =
            new KeyLifetimeActionModel(
                    new KeyLifetimeActionTypeModel(LifetimeActionType.NOTIFY),
                    new KeyLifetimeActionTriggerModel(TRIGGER_30_DAYS_BEFORE_EXPIRY));
    private static final String POLICY_URI_STRING = "https:/localhost:8443/keys/key-name/rotationpolicy";
    private static final String MINIMUM_JSON = "/key/rotation/valid-rotation-policy-minimum.json";
    private static final String FULL_JSON = "/key/rotation/valid-rotation-policy-full.json";
    private static final Period EXPIRY_PERIOD_4M = Period.ofMonths(4);
    private static final OffsetDateTime CREATED_ON = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1482188947), ZoneOffset.UTC);
    private static final OffsetDateTime UPDATED_ON = OffsetDateTime.ofInstant(Instant.ofEpochSecond(1482188948), ZoneOffset.UTC);
    @Autowired
    private Validator validator;
    @Autowired
    private ObjectMapper objectMapper;

    public static Stream<Arguments> invalidProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("/key/rotation/invalid-rotation-policy-empty-actions.json", "lifetimeActions"))
                .add(Arguments.of("/key/rotation/invalid-rotation-policy-missing-attributes.json", "attributes"))
                .add(Arguments.of("/key/rotation/invalid-rotation-policy-missing-expiry.json", "attributes.expiryTime"))
                .add(Arguments.of("/key/rotation/invalid-rotation-policy-missing-actions.json", "lifetimeActions"))
                .add(Arguments.of("/key/rotation/invalid-rotation-policy-null-action-type.json", "lifetimeActions[0].action.type"))
                .add(Arguments.of("/key/rotation/invalid-rotation-policy-null-trigger.json", "lifetimeActions[0].trigger"))
                .build();
    }

    @Test
    void testJsonSerializationShouldContainAllValuableFieldsWhenCalledOnFullyPopulatedObject() throws JsonProcessingException {
        //given
        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(URI.create(POLICY_URI_STRING));
        model.setLifetimeActions(List.of(ROTATE_ACTION, NOTIFY_ACTION));
        model.setAttributes(policyAttributes(EXPIRY_PERIOD_4M));
        final String expected = readResourceAsStringRemoveWhitespace(FULL_JSON);

        //when
        final String actual = objectMapper.writer().writeValueAsString(model);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testJsonSerializationShouldContainAllValuableFieldsWhenCalledOnMinimalObject() throws JsonProcessingException {
        //given
        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(URI.create(POLICY_URI_STRING));
        model.setLifetimeActions(List.of(ROTATE_ACTION));
        model.setAttributes(policyAttributes(null));
        final String expected = readResourceAsStringRemoveWhitespace(MINIMUM_JSON);

        //when
        final String actual = objectMapper.writer().writeValueAsString(model);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void testJsonDeserializationShouldRestoreAllValuableFieldsWhenCalledWithFullyPopulatedJson() throws IOException {
        //given

        //when
        final KeyRotationPolicyModel actual = loadResourceAsObject(FULL_JSON);

        //then
        Assertions.assertEquals(POLICY_URI_STRING, actual.getId().toString());
        Assertions.assertEquals(EXPIRY_PERIOD_4M, actual.getAttributes().getExpiryTime());
        Assertions.assertEquals(CREATED_ON, actual.getAttributes().getCreatedOn());
        Assertions.assertEquals(UPDATED_ON, actual.getAttributes().getUpdatedOn());
        Assertions.assertIterableEquals(List.of(ROTATE_ACTION, NOTIFY_ACTION), actual.getLifetimeActions());
    }


    @Test
    void testJsonDeserializationShouldRestoreAllValuableFieldsWhenCalledWithMinimalJson() throws IOException {
        //given

        //when
        final KeyRotationPolicyModel actual = loadResourceAsObject(MINIMUM_JSON);

        //then
        Assertions.assertEquals(POLICY_URI_STRING, actual.getId().toString());
        Assertions.assertNull(actual.getAttributes().getExpiryTime());
        Assertions.assertEquals(CREATED_ON, actual.getAttributes().getCreatedOn());
        Assertions.assertEquals(UPDATED_ON, actual.getAttributes().getUpdatedOn());
        Assertions.assertIterableEquals(List.of(ROTATE_ACTION), actual.getLifetimeActions());
    }

    @ParameterizedTest
    @MethodSource("invalidProvider")
    void testValidateShouldReportViolationsWhenCalledWithInvalidData(
            final String resource, final String expectedPath) throws IOException {
        //given
        final KeyRotationPolicyModel underTest = loadResourceAsObject(resource);

        //when
        final Set<ConstraintViolation<KeyRotationPolicyModel>> violations = validator.validate(underTest);

        //then
        Assertions.assertEquals(1, violations.size());
        Assertions.assertEquals(expectedPath, violations.iterator().next().getPropertyPath().toString());
    }

    private KeyRotationPolicyAttributes policyAttributes(
            final Period expiryTime) {
        final KeyRotationPolicyAttributes attributes = new KeyRotationPolicyAttributes();
        attributes.setExpiryTime(expiryTime);
        attributes.setCreatedOn(CREATED_ON);
        attributes.setUpdatedOn(UPDATED_ON);
        return attributes;
    }

    private KeyRotationPolicyModel loadResourceAsObject(final String resource) throws IOException {
        final String json = ResourceUtils.loadResourceAsString(resource);
        return objectMapper.reader().readValue(json, KeyRotationPolicyModel.class);
    }

    private String readResourceAsStringRemoveWhitespace(final String resource) {
        final String json = ResourceUtils.loadResourceAsString(resource);
        return Objects.requireNonNull(json).replaceAll("[ \\n]+", "");
    }
}
