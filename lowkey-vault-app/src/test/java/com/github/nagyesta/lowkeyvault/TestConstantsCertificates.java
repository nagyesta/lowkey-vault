package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;

@SuppressWarnings("checkstyle:JavadocVariable")
public final class TestConstantsCertificates {

    private TestConstantsCertificates() {
        throw new IllegalCallerException("Utility.");
    }

    //<editor-fold defaultstate="collapsed" desc="Certificates">
    public static final String CERT_NAME_1 = "cert-name-01";
    public static final String CERT_NAME_2 = "cert-name-02";
    public static final String CERT_NAME_3 = "cert-name-03";
    public static final String CERT_VERSION_1 = "00000000000000000000000000000001";
    public static final String CERT_VERSION_2 = "00000000000000000000000000000002";
    public static final String CERT_VERSION_3 = "00000000000000000000000000000003";
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Certificates - Ids">
    public static final CertificateEntityId UNVERSIONED_CERT_ENTITY_ID_1
            = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
    public static final CertificateEntityId UNVERSIONED_CERT_ENTITY_ID_2
            = new CertificateEntityId(HTTPS_LOWKEY_VAULT, CERT_NAME_2);
    public static final CertificateEntityId UNVERSIONED_CERT_ENTITY_ID_3
            = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_3);

    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_1_VERSION_1
            = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1, CERT_VERSION_1);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_1_VERSION_2
            = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1, CERT_VERSION_2);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_1_VERSION_3
            = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1, CERT_VERSION_3);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_2_VERSION_1
            = new VersionedCertificateEntityId(HTTPS_LOWKEY_VAULT, CERT_NAME_2, CERT_VERSION_1);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_2_VERSION_2
            = new VersionedCertificateEntityId(HTTPS_LOWKEY_VAULT, CERT_NAME_2, CERT_VERSION_2);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_2_VERSION_3
            = new VersionedCertificateEntityId(HTTPS_LOWKEY_VAULT, CERT_NAME_2, CERT_VERSION_3);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_3_VERSION_1
            = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_3, CERT_VERSION_1);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_3_VERSION_2
            = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_3, CERT_VERSION_2);
    public static final VersionedCertificateEntityId VERSIONED_CERT_ENTITY_ID_3_VERSION_3
            = new VersionedCertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_3, CERT_VERSION_3);
    //</editor-fold>
}
