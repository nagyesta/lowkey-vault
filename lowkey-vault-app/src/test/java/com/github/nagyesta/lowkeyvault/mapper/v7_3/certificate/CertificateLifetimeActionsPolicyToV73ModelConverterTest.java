package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertificateLifetimeActionPolicy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.UNVERSIONED_CERT_ENTITY_ID_1;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.AUTO_RENEW;
import static org.mockito.Mockito.mock;

class CertificateLifetimeActionsPolicyToV73ModelConverterTest {

    public static final CertificateLifetimeActionTrigger TRIGGER_AT_80_PERCENT =
            new CertificateLifetimeActionTrigger(CertificateLifetimeActionTriggerType.LIFETIME_PERCENTAGE, 80);

    @Test
    void testConvertShouldConvertPolicyToListWhenCalledWithValidData() {
        //given
        final var registry = mock(CertificateConverterRegistry.class);
        final var underTest =
                new CertificateLifetimeActionsPolicyToV73ModelConverter(registry);
        final var source = new CertificateLifetimeActionPolicy(
                UNVERSIONED_CERT_ENTITY_ID_1, Map.of(AUTO_RENEW, TRIGGER_AT_80_PERCENT));

        //when
        final var actual = underTest.convert(source);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.size());
        Assertions.assertEquals(AUTO_RENEW, actual.get(0).getAction());
        Assertions.assertEquals(TRIGGER_AT_80_PERCENT, actual.get(0).getTrigger().asTriggerEntity());
    }

}
