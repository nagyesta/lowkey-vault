package com.github.nagyesta.lowkeyvault.service.certificate;

import java.time.OffsetDateTime;

public record CertificateLifetimeActionTrigger(
        CertificateLifetimeActionTriggerType triggerType,
        int value) {

    public void validate(final int validityMonths) {
        triggerType.validate(validityMonths, value);
    }

    public long triggersAfterDays(
            final OffsetDateTime validityStart,
            final OffsetDateTime expiry) {
        return triggerType.triggersAfterDays(validityStart, expiry, value);
    }

}
