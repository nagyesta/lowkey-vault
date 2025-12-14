package com.github.nagyesta.lowkeyvault.service.certificate;

import com.github.nagyesta.lowkeyvault.service.common.TimeAware;
import lombok.NonNull;

import java.time.OffsetDateTime;
import java.util.Map;

public interface LifetimeActionPolicy
        extends ReadOnlyLifetimeActionPolicy, TimeAware {

    void setCreated(@NonNull OffsetDateTime created);

    void setUpdated(@NonNull OffsetDateTime updated);

    void setLifetimeActions(Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> lifetimeActions);
}
