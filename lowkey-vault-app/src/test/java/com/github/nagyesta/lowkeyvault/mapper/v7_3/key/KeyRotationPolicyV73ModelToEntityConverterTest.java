package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Period;
import java.util.List;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static com.github.nagyesta.lowkeyvault.TestConstants.TIME_10_MINUTES_AGO;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;

class KeyRotationPolicyV73ModelToEntityConverterTest {

    @Test
    void testConvertShouldConvertValuableFieldsWhenCalledWithValidData() {
        //given
        final var keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final var timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);
        final var expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);

        final var model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setAttributes(attributes(expiryTime));
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));
        model.setKeyEntityId(keyEntityId);

        final var underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final var actual = underTest.convert(model);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyEntityId, actual.getId());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getCreated());
        Assertions.assertEquals(NOW, actual.getUpdated());
        Assertions.assertEquals(expiryTime, actual.getExpiryTime());
        final var actualNotify = actual.getLifetimeActions().get(LifetimeActionType.NOTIFY);
        Assertions.assertEquals(timeBeforeExpiry, actualNotify.trigger().timePeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actualNotify.trigger().triggerType());
        Assertions.assertEquals(LifetimeActionType.NOTIFY, actualNotify.actionType());
    }

    @Test
    void testConvertShouldUseDefaultsWhenCalledWithMinimalAttributes() {
        //given
        final var keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final var expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);
        final var timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);

        final var model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));
        final var attributes = new KeyRotationPolicyAttributes();
        attributes.setExpiryTime(expiryTime);
        model.setAttributes(attributes);
        model.setKeyEntityId(keyEntityId);

        final var underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final var actual = underTest.convert(model);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyEntityId, actual.getId());
        Assertions.assertTrue(actual.getCreated().isAfter(NOW));
        Assertions.assertTrue(actual.getUpdated().isAfter(NOW));
        Assertions.assertEquals(expiryTime, actual.getExpiryTime());
        final var actualNotify = actual.getLifetimeActions().get(LifetimeActionType.NOTIFY);
        Assertions.assertEquals(timeBeforeExpiry, actualNotify.trigger().timePeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actualNotify.trigger().triggerType());
        Assertions.assertEquals(LifetimeActionType.NOTIFY, actualNotify.actionType());
    }

    @Test
    void testConvertShouldThrowExceptionWhenCalledWithoutAttributes() {
        //given
        final var keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final var timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);

        final var model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));
        model.setKeyEntityId(keyEntityId);

        final var underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.convert(model));

        //then + exception
    }

    @Test
    void testConvertShouldReturnNullWhenCalledWithEmptyModel() {
        //given
        final var keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        final var model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setKeyEntityId(keyEntityId);

        final var underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final var actual = underTest.convert(model);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testConvertShouldReturnNullWhenCalledWithoutAttributesAndEmptyList() {
        //given
        final var keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        final var model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setLifetimeActions(List.of());
        model.setKeyEntityId(keyEntityId);

        final var underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final var actual = underTest.convert(model);

        //then
        Assertions.assertNull(actual);
    }

    private KeyLifetimeActionModel notifyAction(final Period timeBeforeExpiry) {
        final var notify = new KeyLifetimeActionModel();
        notify.setTrigger(new KeyLifetimeActionTriggerModel(timeBeforeExpiry, null));
        notify.setAction(new KeyLifetimeActionTypeModel(LifetimeActionType.NOTIFY));
        return notify;
    }

    private KeyRotationPolicyAttributes attributes(final Period expiryTime) {
        final var attributes = new KeyRotationPolicyAttributes();
        attributes.setCreated(TIME_10_MINUTES_AGO);
        attributes.setUpdated(NOW);
        attributes.setExpiryTime(expiryTime);
        return attributes;
    }
}
