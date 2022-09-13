package com.github.nagyesta.lowkeyvault.service.certificate.impl;

public enum CertAuthorityType {

    /**
     * Self-signed certificate.
     */
    SELF_SIGNED("Self"),
    /**
     * Unknown, imported certificate.
     */
    UNKNOWN("Unknown");

    private final String value;

    CertAuthorityType(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
