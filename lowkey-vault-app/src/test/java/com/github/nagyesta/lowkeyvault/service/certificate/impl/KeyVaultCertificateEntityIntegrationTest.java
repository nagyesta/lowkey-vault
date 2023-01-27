package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.SubjectAlternativeNames;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.X509CertificateModel;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.MimeTypeUtils;

import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstants.LOWKEY_VAULT;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificateKeys.EMPTY_PASSWORD;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;

class KeyVaultCertificateEntityIntegrationTest {
    public static final int TWO_YEARS_IN_MONTHS = 24;

    @Test
    void testImportConstructorShouldGenerateMatchingVersionsWhenCalledWithValidInput() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final JsonWebKeyImportRequest keyData = input.getKeyData();
        final CertificateCreationInput certificateData = input.getCertificateData();
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity entity = new KeyVaultCertificateEntity(CERT_NAME_1, certificateData, certificate, keyData, vault);

        //then
        Assertions.assertEquals(entity.getId().vault(), entity.getKid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getKid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getKid().version());
        Assertions.assertEquals(entity.getId().vault(), entity.getSid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getSid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getSid().version());
    }

    @Test
    void testImportConstructorShouldOverrideCertificatePolicyDataWhenProvided() throws CertificateParsingException {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final String expectedSubject = "CN=example.com";
        final String expectedSan = "*.localhost";

        final X509CertificateModel x509Properties = new X509CertificateModel();
        x509Properties.setSubject(expectedSubject);
        x509Properties.setSubjectAlternativeNames(new SubjectAlternativeNames(Set.of(expectedSan), Set.of(), Set.of()));
        x509Properties.setExtendedKeyUsage(Set.of());
        x509Properties.setKeyUsage(Set.of(KeyUsageEnum.ENCIPHER_ONLY));
        x509Properties.setValidityMonths(TWO_YEARS_IN_MONTHS);

        final CertificatePolicyModel policyModel = new CertificatePolicyModel();
        policyModel.setX509Properties(x509Properties);

        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, policyModel);
        final JsonWebKeyImportRequest keyData = input.getKeyData();
        final CertificateCreationInput certificateData = input.getCertificateData();
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity actual = new KeyVaultCertificateEntity(CERT_NAME_1, certificateData, certificate, keyData, vault);

        //then
        Assertions.assertEquals(certificate, actual.getCertificate());
        Assertions.assertEquals(expectedSubject, actual.getGenerator().getSubject());
        Assertions.assertIterableEquals(Set.of(expectedSan), actual.getGenerator().getDnsNames());
        Assertions.assertIterableEquals(Set.of(), actual.getGenerator().getIps());
        Assertions.assertIterableEquals(Set.of(), actual.getGenerator().getEmails());
        Assertions.assertIterableEquals(Set.of(), actual.getGenerator().getExtendedKeyUsage());
        Assertions.assertIterableEquals(Set.of(KeyUsageEnum.ENCIPHER_ONLY), actual.getGenerator().getKeyUsage());
        Assertions.assertEquals(TWO_YEARS_IN_MONTHS, actual.getGenerator().getValidityMonths());
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithAlreadyExistingSecret() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final JsonWebKeyImportRequest keyData = input.getKeyData();
        final CertificateCreationInput certificateData = input.getCertificateData();
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        vault.secretVaultFake().createSecretVersion(CERT_NAME_1, LOWKEY_VAULT, MimeTypeUtils.TEXT_PLAIN.getType());

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, certificateData, certificate, keyData, vault));

        //then + exception
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithAlreadyExistingKey() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final JsonWebKeyImportRequest keyData = input.getKeyData();
        final CertificateCreationInput certificateData = input.getCertificateData();
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        vault.keyVaultFake().createEcKeyVersion(CERT_NAME_1, new EcKeyCreationInput(KeyType.EC, KeyCurveName.P_256));

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, certificateData, certificate, keyData, vault));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullName() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final JsonWebKeyImportRequest keyData = input.getKeyData();
        final CertificateCreationInput certificateData = input.getCertificateData();
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(null, certificateData, certificate, keyData, vault));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullCertificateData() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final JsonWebKeyImportRequest keyData = input.getKeyData();
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, null, certificate, keyData, vault));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullCertificate() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final JsonWebKeyImportRequest keyData = input.getKeyData();
        final CertificateCreationInput certificateData = input.getCertificateData();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, certificateData, null, keyData, vault));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullKeyData() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final X509Certificate certificate = input.getCertificate();
        final CertificateCreationInput certificateData = input.getCertificateData();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, certificateData, certificate, null, vault));

        //then + exception
    }
}
