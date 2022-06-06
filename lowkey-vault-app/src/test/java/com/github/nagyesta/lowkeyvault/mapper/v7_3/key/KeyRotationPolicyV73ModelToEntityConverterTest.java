package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.LifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Period;
import java.util.List;

import static com.github.nagyesta.lowkeyvault.TestConstants.NOW;
import static com.github.nagyesta.lowkeyvault.TestConstants.TIME_10_MINUTES_AGO;

class KeyRotationPolicyV73ModelToEntityConverterTest {

    @Test
    void testConvertShouldConvertValuableFieldsWhenCalledWithValidData() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);
        final Period expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri());
        model.setAttributes(attributes(expiryTime));
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));

        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final RotationPolicy actual = underTest.convert(keyEntityId, model);

        //then
        Assertions.assertEquals(keyEntityId, actual.getId());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getCreatedOn());
        Assertions.assertEquals(NOW, actual.getUpdatedOn());
        Assertions.assertEquals(expiryTime, actual.getExpiryTime());
        final LifetimeAction actualNotify = actual.getLifetimeActions().get(LifetimeActionType.NOTIFY);
        Assertions.assertEquals(timeBeforeExpiry, actualNotify.getTrigger().getTimePeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actualNotify.getTrigger().getTriggerType());
        Assertions.assertEquals(LifetimeActionType.NOTIFY, actualNotify.getActionType());
    }

    @Test
    void testConvertShouldUseDefaultsWhenCalledWithMinimalAttributes() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);
        final Period timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri());
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));
        final KeyRotationPolicyAttributes attributes = new KeyRotationPolicyAttributes();
        attributes.setExpiryTime(expiryTime);
        model.setAttributes(attributes);

        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final RotationPolicy actual = underTest.convert(keyEntityId, model);

        //then
        Assertions.assertEquals(keyEntityId, actual.getId());
        Assertions.assertTrue(actual.getCreatedOn().isAfter(NOW));
        Assertions.assertTrue(actual.getUpdatedOn().isAfter(NOW));
        Assertions.assertEquals(expiryTime, actual.getExpiryTime());
        final LifetimeAction actualNotify = actual.getLifetimeActions().get(LifetimeActionType.NOTIFY);
        Assertions.assertEquals(timeBeforeExpiry, actualNotify.getTrigger().getTimePeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actualNotify.getTrigger().getTriggerType());
        Assertions.assertEquals(LifetimeActionType.NOTIFY, actualNotify.getActionType());
    }

    @Test
    void testConvertShouldThrowExceptionWhenCalledWithoutAttributes() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri());
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));

        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.convert(keyEntityId, model));

        //then + exception
    }

    @Test
    void testConvertShouldReturnNullWhenCalledWithEmptyModel() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri());

        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final RotationPolicy actual = underTest.convert(keyEntityId, model);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testConvertShouldThrowExceptionWhenCalledWithNoList() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri());
        model.setAttributes(attributes(expiryTime));

        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.convert(keyEntityId, model));

        //then + exception
    }

    @Test
    void testConvertShouldReturnNullWhenCalledWithoutAttributesAndEmptyList() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri());
        model.setLifetimeActions(List.of());

        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter();

        //when
        final RotationPolicy actual = underTest.convert(keyEntityId, model);

        //then
        Assertions.assertNull(actual);
    }

    private KeyLifetimeActionModel notifyAction(final Period timeBeforeExpiry) {
        final KeyLifetimeActionModel notify = new KeyLifetimeActionModel();
        notify.setTrigger(new KeyLifetimeActionTriggerModel(timeBeforeExpiry, null));
        notify.setAction(new KeyLifetimeActionTypeModel(LifetimeActionType.NOTIFY));
        return notify;
    }

    private KeyRotationPolicyAttributes attributes(final Period expiryTime) {
        final KeyRotationPolicyAttributes attributes = new KeyRotationPolicyAttributes();
        attributes.setCreatedOn(TIME_10_MINUTES_AGO);
        attributes.setUpdatedOn(NOW);
        attributes.setExpiryTime(expiryTime);
        return attributes;
    }
}
