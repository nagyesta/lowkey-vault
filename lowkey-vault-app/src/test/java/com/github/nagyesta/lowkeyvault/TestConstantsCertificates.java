package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyUsageEnum;

import java.util.List;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;

@SuppressWarnings("checkstyle:JavadocVariable")
public final class TestConstantsCertificates {

    private TestConstantsCertificates() {
        throw new IllegalCallerException("Utility.");
    }

    public static final int VALIDITY_MONTHS_ONE_YEAR = 12;
    public static final int VALIDITY_TEN_MONTHS = 10;

    public static final Set<String> EXTENDED_KEY_USAGE = Set.of("1.3.6.1.5.5.7.3.1");
    public static final String CN_TEST = "CN=Test";
    public static final String SELF = "Self";
    public static final boolean CERT_TRANSPARENCY = false;
    public static final String SELF_SIGNED = "SelfSigned";
    public static final boolean ENABLED = true;
    public static final boolean REUSE_KEY = true;
    public static final boolean EXPORTABLE = true;
    public static final int SECONDS_IN_FIVE_DAYS = 5 * 24 * 60 * 60;
    public static final String CERTIFICATE_BACKUP_TEST = "certificate-backup-test";
    public static final Set<String> SANS_TEST_COM_AND_TEST2_COM = Set.of("test.com", "test2.com");
    public static final Set<KeyUsageEnum> KEY_USAGE_ENCIPHER_ONLY_DECIPHER_ONLY = Set
            .of(KeyUsageEnum.ENCIPHER_ONLY, KeyUsageEnum.DECIPHER_ONLY);
    public static final List<KeyOperation> ALL_KEY_OPERATIONS = List.of(
            KeyOperation.ENCRYPT,
            KeyOperation.DECRYPT,
            KeyOperation.SIGN,
            KeyOperation.VERIFY,
            KeyOperation.WRAP_KEY,
            KeyOperation.UNWRAP_KEY);

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
