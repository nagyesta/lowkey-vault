package com.github.nagyesta.lowkeyvault.service.certificate;

import com.github.nagyesta.lowkeyvault.service.common.TimeAware;

import java.time.OffsetDateTime;
import java.util.Map;

public interface LifetimeActionPolicy extends ReadOnlyLifetimeActionPolicy, TimeAware {

    void setCreated(OffsetDateTime created);

    void setUpdated(OffsetDateTime updated);

    void setLifetimeActions(Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> lifetimeActions);
}
