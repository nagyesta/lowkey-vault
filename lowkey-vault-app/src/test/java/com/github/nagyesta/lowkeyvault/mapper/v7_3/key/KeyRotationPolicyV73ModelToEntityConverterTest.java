package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
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
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.mock;

class KeyRotationPolicyV73ModelToEntityConverterTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyRotationPolicyV73ModelToEntityConverter(null));

        //then + exception
    }
    @Test
    void testConvertShouldConvertValuableFieldsWhenCalledWithValidData() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);
        final Period expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setAttributes(attributes(expiryTime));
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));
        model.setKeyEntityId(keyEntityId);

        final KeyConverterRegistry registry = mock(KeyConverterRegistry.class);
        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter(registry);

        //when
        final RotationPolicy actual = underTest.convert(model);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyEntityId, actual.getId());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getCreatedOn());
        Assertions.assertEquals(NOW, actual.getUpdatedOn());
        Assertions.assertEquals(expiryTime, actual.getExpiryTime());
        final LifetimeAction actualNotify = actual.getLifetimeActions().get(LifetimeActionType.NOTIFY);
        Assertions.assertEquals(timeBeforeExpiry, actualNotify.trigger().timePeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actualNotify.trigger().triggerType());
        Assertions.assertEquals(LifetimeActionType.NOTIFY, actualNotify.actionType());
    }

    @Test
    void testConvertShouldUseDefaultsWhenCalledWithMinimalAttributes() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);
        final Period timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));
        final KeyRotationPolicyAttributes attributes = new KeyRotationPolicyAttributes();
        attributes.setExpiryTime(expiryTime);
        model.setAttributes(attributes);
        model.setKeyEntityId(keyEntityId);

        final KeyConverterRegistry registry = mock(KeyConverterRegistry.class);
        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter(registry);

        //when
        final RotationPolicy actual = underTest.convert(model);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyEntityId, actual.getId());
        Assertions.assertTrue(actual.getCreatedOn().isAfter(NOW));
        Assertions.assertTrue(actual.getUpdatedOn().isAfter(NOW));
        Assertions.assertEquals(expiryTime, actual.getExpiryTime());
        final LifetimeAction actualNotify = actual.getLifetimeActions().get(LifetimeActionType.NOTIFY);
        Assertions.assertEquals(timeBeforeExpiry, actualNotify.trigger().timePeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actualNotify.trigger().triggerType());
        Assertions.assertEquals(LifetimeActionType.NOTIFY, actualNotify.actionType());
    }

    @Test
    void testConvertShouldThrowExceptionWhenCalledWithoutAttributes() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period timeBeforeExpiry = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setLifetimeActions(List.of(notifyAction(timeBeforeExpiry)));
        model.setKeyEntityId(keyEntityId);

        final KeyConverterRegistry registry = mock(KeyConverterRegistry.class);
        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter(registry);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.convert(model));

        //then + exception
    }

    @Test
    void testConvertShouldReturnNullWhenCalledWithEmptyModel() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setKeyEntityId(keyEntityId);

        final KeyConverterRegistry registry = mock(KeyConverterRegistry.class);
        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter(registry);

        //when
        final RotationPolicy actual = underTest.convert(model);

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testConvertShouldThrowExceptionWhenCalledWithNoList() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setAttributes(attributes(expiryTime));
        model.setKeyEntityId(keyEntityId);

        final KeyConverterRegistry registry = mock(KeyConverterRegistry.class);
        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter(registry);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.convert(model));

        //then + exception
    }

    @Test
    void testConvertShouldReturnNullWhenCalledWithoutAttributesAndEmptyList() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        final KeyRotationPolicyModel model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443));
        model.setLifetimeActions(List.of());
        model.setKeyEntityId(keyEntityId);

        final KeyConverterRegistry registry = mock(KeyConverterRegistry.class);
        final KeyRotationPolicyV73ModelToEntityConverter underTest = new KeyRotationPolicyV73ModelToEntityConverter(registry);

        //when
        final RotationPolicy actual = underTest.convert(model);

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
        attributes.setCreated(TIME_10_MINUTES_AGO);
        attributes.setUpdated(NOW);
        attributes.setExpiryTime(expiryTime);
        return attributes;
    }
}
