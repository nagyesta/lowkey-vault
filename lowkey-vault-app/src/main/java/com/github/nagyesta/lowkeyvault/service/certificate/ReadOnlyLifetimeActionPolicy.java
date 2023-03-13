package com.github.nagyesta.lowkeyvault.service.certificate;

import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface ReadOnlyLifetimeActionPolicy {

    CertificateEntityId getId();

    OffsetDateTime getCreatedOn();

    OffsetDateTime getUpdatedOn();

    Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> getLifetimeActions();

    boolean isAutoRenew();

    void validate(int validityMonths);

    List<OffsetDateTime> missedRenewalDays(OffsetDateTime validityStart, Function<OffsetDateTime, OffsetDateTime> createdToExpiryFunction);
}
