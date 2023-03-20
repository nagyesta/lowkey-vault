package com.github.nagyesta.lowkeyvault.mapper.v7_3.key;

import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyLifetimeActionModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType;
import com.github.nagyesta.lowkeyvault.service.key.KeyLifetimeAction;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyRotationPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Period;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.TIME_10_MINUTES_AGO;
import static com.github.nagyesta.lowkeyvault.TestConstants.TIME_IN_10_MINUTES;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.mock;

class KeyRotationPolicyToV73ModelConverterTest {

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyRotationPolicyToV73ModelConverter(null));

        //then + exception
    }

    @Test
    void testConvertShouldConvertValuableFieldsWhenCalledWithValidData() {
        //given
        final KeyEntityId keyEntityId = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;
        final Period expiryTime = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);
        final Period triggerPeriod = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);

        final KeyLifetimeActionTrigger trigger = new KeyLifetimeActionTrigger(triggerPeriod, LifetimeActionTriggerType.TIME_BEFORE_EXPIRY);
        final KeyRotationPolicy source = new KeyRotationPolicy(keyEntityId, expiryTime,
                Map.of(LifetimeActionType.NOTIFY, new KeyLifetimeAction(LifetimeActionType.NOTIFY, trigger)));
        source.setCreatedOn(TIME_10_MINUTES_AGO);
        source.setUpdatedOn(TIME_IN_10_MINUTES);
        final KeyConverterRegistry registry = mock(KeyConverterRegistry.class);

        final KeyRotationPolicyToV73ModelConverter underTest = new KeyRotationPolicyToV73ModelConverter(registry);

        //when
        final KeyRotationPolicyModel actual = underTest.convert(source, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyEntityId.asRotationPolicyUri(HTTPS_LOCALHOST_8443), actual.getId());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actual.getAttributes().getCreated());
        Assertions.assertEquals(TIME_IN_10_MINUTES, actual.getAttributes().getUpdated());
        Assertions.assertEquals(expiryTime, actual.getAttributes().getExpiryTime());
        Assertions.assertEquals(1, actual.getLifetimeActions().size());
        final KeyLifetimeActionModel actionModel = actual.getLifetimeActions().get(0);
        Assertions.assertEquals(triggerPeriod, actionModel.getTrigger().getTriggerPeriod());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_BEFORE_EXPIRY, actionModel.getTrigger().getTriggerType());
        Assertions.assertEquals(LifetimeActionType.NOTIFY, actionModel.getAction().getType());
    }
}
