package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificateKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.ALL_KEY_OPERATIONS;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;

class CertificateImportInputTest {

    private static final int MONTHS_1_YEAR = 12;
    private static final int MONTHS_10_YEARS = 120;
    private static final String IP_LOCALHOST = "127.0.0.1";
    private static final String DNS_WILDCARD_LOCALHOST = "*.localhost";
    private static final String EMAIL_JOHN_DOE = "john.doe@example.com";
    private static final String CN_EC_LOCALHOST = "CN=ec.localhost";
    private static final String CN_ALT_EC_LOCALHOST = "CN=alt.ec.localhost";
    private static final String EKU_1 = "1.3.6.1.5.5.7.3.1";


    @Test
    void testConstructorShouldUseValuesFromCertificateWhenCalledWithMinimalEcDataUsingPkcs12() {
        //given
        final var certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/ec.p12"));

        //when
        final var actual = new CertificateImportInput(
                CERT_NAME_1, certContent, PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());

        //then
        Assertions.assertEquals(CERT_NAME_1, actual.getCertificateData().getName());
        Assertions.assertEquals(CN_EC_LOCALHOST, actual.getCertificateData().getSubject());
        Assertions.assertEquals(KeyType.EC, actual.getCertificateData().getKeyType());
        Assertions.assertEquals(KeyCurveName.P_256, actual.getCertificateData().getKeyCurveName());
        Assertions.assertNull(actual.getCertificateData().getKeySize());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getDnsNames());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getUpns());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getEmails());
        Assertions.assertEquals(MONTHS_1_YEAR, actual.getCertificateData().getValidityMonths());
        Assertions.assertEquals(CertContentType.PKCS12, actual.getCertificateData().getContentType());
        Assertions.assertFalse(actual.getCertificateData().isReuseKeyOnRenewal());
        Assertions.assertFalse(actual.getCertificateData().isExportablePrivateKey());
        Assertions.assertFalse(actual.getCertificateData().isEnableTransparency());
    }

    @Test
    void testConstructorShouldUseValuesFromCertificateWhenCalledWithMinimalEcDataUsingPem() {
        //given
        final var certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsString("/cert/ec.pem"));

        //when
        final var actual = new CertificateImportInput(
                CERT_NAME_1, certContent, PASSWORD, CertContentType.PEM, new CertificatePolicyModel());

        //then
        Assertions.assertEquals(CERT_NAME_1, actual.getCertificateData().getName());
        Assertions.assertEquals(CN_EC_LOCALHOST, actual.getCertificateData().getSubject());
        Assertions.assertEquals(KeyType.EC, actual.getCertificateData().getKeyType());
        Assertions.assertEquals(KeyCurveName.P_256, actual.getCertificateData().getKeyCurveName());
        Assertions.assertNull(actual.getCertificateData().getKeySize());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getDnsNames());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getUpns());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getEmails());
        Assertions.assertEquals(MONTHS_1_YEAR, actual.getCertificateData().getValidityMonths());
        Assertions.assertEquals(CertContentType.PEM, actual.getCertificateData().getContentType());
        Assertions.assertFalse(actual.getCertificateData().isReuseKeyOnRenewal());
        Assertions.assertFalse(actual.getCertificateData().isExportablePrivateKey());
        Assertions.assertFalse(actual.getCertificateData().isEnableTransparency());
    }

    @Test
    void testConstructorShouldUseValuesFromParsedCertOnlyWhenCalledWithMixedEcDataUsingPem() {
        //given
        final var certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsString("/cert/ec.pem"));

        final var policyModel = new CertificatePolicyModel();
        final var keyProperties = new CertificateKeyModel();
        keyProperties.setExportable(true);
        keyProperties.setReuseKey(true);
        keyProperties.setKeyType(KeyType.EC_HSM);
        keyProperties.setKeyCurveName(KeyCurveName.P_256K);
        policyModel.setKeyProperties(keyProperties);

        final var x509Properties = new X509CertificateModel();
        x509Properties.setSubject(CN_ALT_EC_LOCALHOST);
        x509Properties.setKeyUsage(Set.of(KeyUsageEnum.ENCIPHER_ONLY));
        x509Properties.setExtendedKeyUsage(Set.of(EKU_1));
        x509Properties.setValidityMonths(MONTHS_10_YEARS);
        x509Properties.setSubjectAlternativeNames(
                new SubjectAlternativeNames(Set.of(DNS_WILDCARD_LOCALHOST), Set.of(EMAIL_JOHN_DOE), Set.of(IP_LOCALHOST)));
        policyModel.setX509Properties(x509Properties);

        final var secretProperties = new CertificateSecretModel();
        secretProperties.setContentType(CertContentType.PEM.getMimeType());
        policyModel.setSecretProperties(secretProperties);

        final var issuer = new IssuerParameterModel();
        issuer.setCertTransparency(true);
        policyModel.setIssuer(issuer);

        //when
        final var actual = new CertificateImportInput(
                CERT_NAME_1, certContent, PASSWORD, CertContentType.PEM, policyModel);

        //then
        Assertions.assertEquals(CERT_NAME_1, actual.getCertificateData().getName());
        Assertions.assertEquals(CN_EC_LOCALHOST, actual.getCertificateData().getSubject());
        Assertions.assertEquals(KeyType.EC_HSM, actual.getCertificateData().getKeyType());
        Assertions.assertEquals(KeyCurveName.P_256K, actual.getCertificateData().getKeyCurveName());
        Assertions.assertNull(actual.getCertificateData().getKeySize());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getDnsNames());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getUpns());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getEmails());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getKeyUsage());
        Assertions.assertIterableEquals(Set.of(), actual.getCertificateData().getExtendedKeyUsage());
        Assertions.assertEquals(MONTHS_1_YEAR, actual.getCertificateData().getValidityMonths());
        Assertions.assertEquals(CertContentType.PEM, actual.getCertificateData().getContentType());
        Assertions.assertTrue(actual.getCertificateData().isReuseKeyOnRenewal());
        Assertions.assertTrue(actual.getCertificateData().isExportablePrivateKey());
        Assertions.assertTrue(actual.getCertificateData().isEnableTransparency());
    }

    @Test
    void testConstructorShouldUseValuesFromKeyWhenCalledWithMinimalEcDataUsingPkcs12() {
        //given
        final var certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/ec.p12"));

        //when
        final var actual = new CertificateImportInput(
                CERT_NAME_1, certContent, PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());

        //then
        Assertions.assertEquals(KeyType.EC, actual.getKeyData().getKeyType());
        Assertions.assertIterableEquals(Set.of(), actual.getKeyData().getKeyOps());
        Assertions.assertArrayEquals(EC_KEY_X, actual.getKeyData().getX());
        Assertions.assertArrayEquals(EC_KEY_Y, actual.getKeyData().getY());
        Assertions.assertArrayEquals(EC_KEY_D, actual.getKeyData().getD());
        Assertions.assertEquals(KeyCurveName.P_256, actual.getKeyData().getCurveName());
    }

    @Test
    void testConstructorShouldUseValuesFromKeyWhenCalledWithMinimalRsaDataUsingPkcs12() {
        //given
        final var certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));

        //when
        final var actual = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());

        //then
        Assertions.assertEquals(KeyType.RSA, actual.getKeyData().getKeyType());
        Assertions.assertIterableEquals(ALL_KEY_OPERATIONS, actual.getKeyData().getKeyOps());
        Assertions.assertArrayEquals(RSA_KEY_E, actual.getKeyData().getE());
        Assertions.assertArrayEquals(RSA_KEY_N, actual.getKeyData().getN());
        Assertions.assertArrayEquals(RSA_KEY_D, actual.getKeyData().getD());
        Assertions.assertArrayEquals(RSA_KEY_DP, actual.getKeyData().getDp());
        Assertions.assertArrayEquals(RSA_KEY_DQ, actual.getKeyData().getDq());
        Assertions.assertArrayEquals(RSA_KEY_Q, actual.getKeyData().getQ());
        Assertions.assertArrayEquals(RSA_KEY_QI, actual.getKeyData().getQi());
        Assertions.assertNull(actual.getKeyData().getK());
        Assertions.assertNull(actual.getKeyData().getCurveName());
    }

    @Test
    void testConstructorShouldUseValuesFromKeyWhenCalledWithMinimalRsaDataUsingPem() {
        //given
        final var certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsString("/cert/rsa.pem"));

        //when
        final var actual = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PEM, new CertificatePolicyModel());

        //then
        Assertions.assertEquals(KeyType.RSA, actual.getKeyData().getKeyType());
        Assertions.assertIterableEquals(ALL_KEY_OPERATIONS, actual.getKeyData().getKeyOps());
        Assertions.assertArrayEquals(RSA_KEY_E, actual.getKeyData().getE());
        Assertions.assertArrayEquals(RSA_KEY_N, actual.getKeyData().getN());
        Assertions.assertArrayEquals(RSA_KEY_D, actual.getKeyData().getD());
        Assertions.assertArrayEquals(RSA_KEY_DP, actual.getKeyData().getDp());
        Assertions.assertArrayEquals(RSA_KEY_DQ, actual.getKeyData().getDq());
        Assertions.assertArrayEquals(RSA_KEY_Q, actual.getKeyData().getQ());
        Assertions.assertArrayEquals(RSA_KEY_QI, actual.getKeyData().getQi());
        Assertions.assertNull(actual.getKeyData().getK());
        Assertions.assertNull(actual.getKeyData().getCurveName());
    }
}
