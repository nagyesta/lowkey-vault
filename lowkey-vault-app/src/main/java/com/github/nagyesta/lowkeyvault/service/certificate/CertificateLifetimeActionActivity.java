package com.github.nagyesta.lowkeyvault.service.certificate;

import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;

@Getter
public enum CertificateLifetimeActionActivity {
    /**
     * Noop, simulates email notification actions.
     */
    EMAIL_CONTACTS("EmailContacts"),
    /**
     * Performs automatic renewal of the certificate.
     */
    AUTO_RENEW("AutoRenew");

    private final String value;

    CertificateLifetimeActionActivity(final String value) {
        this.value = value;
    }

    public static CertificateLifetimeActionActivity byValue(@NonNull final String value) {
        return Arrays.stream(CertificateLifetimeActionActivity.values())
                .filter(v -> v.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown lifetime action activity: " + value));
    }

}
