package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTrigger;
import com.github.nagyesta.lowkeyvault.service.certificate.LifetimeActionPolicy;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.impl.BaseLifetimePolicy;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class CertificateLifetimeActionPolicy extends BaseLifetimePolicy<CertificateEntityId> implements LifetimeActionPolicy {

    private Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> lifetimeActions;

    public CertificateLifetimeActionPolicy(
            @org.springframework.lang.NonNull final CertificateEntityId certificateEntityId,
            @NonNull final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> lifetimeActions) {
        super(certificateEntityId);
        this.lifetimeActions = Map.copyOf(lifetimeActions);
    }

    @Override
    public Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> getLifetimeActions() {
        return lifetimeActions;
    }

    @Override
    public void setLifetimeActions(final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> lifetimeActions) {
        this.lifetimeActions = Map.copyOf(lifetimeActions);
        this.markUpdate();
    }

    @Override
    public boolean isAutoRenew() {
        return getLifetimeActions().containsKey(CertificateLifetimeActionActivity.AUTO_RENEW);
    }

    @Override
    public void validate(final int validityMonths) {
        lifetimeActions.values().forEach(a -> a.validate(validityMonths));
    }

    @Override
    public List<OffsetDateTime> missedRenewalDays(final OffsetDateTime validityStart, final OffsetDateTime expiry) {
        Assert.isTrue(isAutoRenew(), "Cannot have missed renewals without an \"AutoRenew\" lifetime action.");
        final long triggersAfterDays = lifetimeActions.get(CertificateLifetimeActionActivity.AUTO_RENEW)
                .triggersAfterDays(validityStart, expiry);
        final OffsetDateTime startPoint = findTriggerTimeOffset(validityStart, triggersAfterDays);
        return collectMissedTriggerDays(triggersAfterDays, startPoint);
    }

}
