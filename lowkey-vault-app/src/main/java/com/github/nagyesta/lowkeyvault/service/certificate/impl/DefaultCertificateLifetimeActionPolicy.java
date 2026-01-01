package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import org.springframework.util.Assert;

import java.util.Map;

import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.LIFETIME_PERCENTAGE;

public class DefaultCertificateLifetimeActionPolicy
        extends CertificateLifetimeActionPolicy {

    private static final int DEFAULT_TRIGGER_PERCENTAGE = 80;

    public DefaultCertificateLifetimeActionPolicy(
            final CertificateEntityId certificateEntityId,
            final CertAuthorityType certAuthorityType) {
        super(certificateEntityId, defaultMapForType(certAuthorityType));
    }

    private static Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> defaultMapForType(
            final CertAuthorityType certAuthorityType) {
        Assert.notNull(certAuthorityType, "CertAuthorityType cannot be null.");
        return Map.of(certAuthorityType.getDefaultAction(),
                new CertificateLifetimeActionTrigger(LIFETIME_PERCENTAGE, DEFAULT_TRIGGER_PERCENTAGE));
    }

}
