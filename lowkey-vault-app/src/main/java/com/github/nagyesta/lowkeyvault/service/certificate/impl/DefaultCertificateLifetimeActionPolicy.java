package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import lombok.NonNull;

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
            @NonNull final CertAuthorityType certAuthorityType) {
        return Map.of(certAuthorityType.getDefaultAction(),
                new CertificateLifetimeActionTrigger(LIFETIME_PERCENTAGE, DEFAULT_TRIGGER_PERCENTAGE));
    }

}
