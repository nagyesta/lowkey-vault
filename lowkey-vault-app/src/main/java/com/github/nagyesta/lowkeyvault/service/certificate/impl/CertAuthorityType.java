package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum CertAuthorityType {

    /**
     * Self-signed certificate.
     */
    SELF_SIGNED("Self", CertificateLifetimeActionActivity.AUTO_RENEW),
    /**
     * Unknown, imported certificate.
     */
    UNKNOWN("Unknown", CertificateLifetimeActionActivity.EMAIL_CONTACTS);

    private final String value;
    private final CertificateLifetimeActionActivity defaultAction;

    CertAuthorityType(
            final String value,
            final CertificateLifetimeActionActivity action) {
        this.value = value;
        this.defaultAction = action;
    }

    public static CertAuthorityType byValue(final String value) {
        return Arrays.stream(values())
                .filter(c -> c.getValue().equals(value))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
