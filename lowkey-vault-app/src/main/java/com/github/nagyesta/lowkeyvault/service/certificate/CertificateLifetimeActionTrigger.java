package com.github.nagyesta.lowkeyvault.service.certificate;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.time.OffsetDateTime;

@Getter
@EqualsAndHashCode
public class CertificateLifetimeActionTrigger {

    private final int value;
    private final CertificateLifetimeActionTriggerType triggerType;

    public CertificateLifetimeActionTrigger(
            @NonNull final CertificateLifetimeActionTriggerType triggerType, final int value) {
        this.triggerType = triggerType;
        this.value = value;
    }

    public void validate(final int validityMonths) {
        triggerType.validate(validityMonths, value);
    }

    public long triggersAfterDays(final OffsetDateTime validityStart, final OffsetDateTime expiry) {
        return triggerType.triggersAfterDays(validityStart, expiry, value);
    }

}
