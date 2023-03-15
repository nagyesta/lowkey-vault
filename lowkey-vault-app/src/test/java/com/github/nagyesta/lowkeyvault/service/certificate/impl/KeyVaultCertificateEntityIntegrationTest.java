package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.SubjectAlternativeNames;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.X509CertificateModel;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.util.MimeTypeUtils;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificateKeys.EMPTY_PASSWORD;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.SELF_SIGNED;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

class KeyVaultCertificateEntityIntegrationTest {
    public static final int TWO_YEARS_IN_MONTHS = 24;
    public static final int THIRTY_YEARS_IN_MONTHS = 360;

    @Test
    void testImportConstructorShouldGenerateMatchingVersionsWhenCalledWithValidInput() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity entity = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        Assertions.assertEquals(entity.getId().vault(), entity.getKid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getKid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getKid().version());
        Assertions.assertEquals(entity.getId().vault(), entity.getSid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getSid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getSid().version());
    }

    @Test
    void testImportConstructorShouldIgnoreMismatchInCertificatePolicyDataWhenProvided() {
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
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity actual = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        Assertions.assertEquals(certificate, actual.getCertificate());
        final ReadOnlyCertificatePolicy originalPolicy = actual.getOriginalCertificatePolicy();
        Assertions.assertEquals("CN=localhost", originalPolicy.getSubject());
        Assertions.assertIterableEquals(
                new TreeSet<>(Set.of("localhost", "127.0.0.1")),
                new TreeSet<>(originalPolicy.getDnsNames()));
        Assertions.assertIterableEquals(Set.of(), originalPolicy.getUpns());
        Assertions.assertIterableEquals(Set.of(), originalPolicy.getEmails());
        Assertions.assertIterableEquals(
                new TreeSet<>(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2")),
                new TreeSet<>(originalPolicy.getExtendedKeyUsage()));
        Assertions.assertIterableEquals(
                new TreeSet<>(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT, KeyUsageEnum.DIGITAL_SIGNATURE)),
                new TreeSet<>(originalPolicy.getKeyUsage()));
        Assertions.assertEquals(THIRTY_YEARS_IN_MONTHS, originalPolicy.getValidityMonths());
        Assertions.assertNotNull(actual.getOriginalCertificateContents());
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithAlreadyExistingSecret() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        vault.secretVaultFake().createSecretVersion(CERT_NAME_1, LOWKEY_VAULT, MimeTypeUtils.TEXT_PLAIN.getType());

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));

        //then + exception
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithAlreadyExistingKey() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        vault.keyVaultFake().createEcKeyVersion(CERT_NAME_1, new EcKeyCreationInput(KeyType.EC, KeyCurveName.P_256));

        //when
        Assertions.assertThrows(IllegalStateException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullName() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(null, input, vault));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullCertificateImportInput() {
        //given
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, (CertificateImportInput) null, vault));

        //then + exception
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullCertificateData() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = spy(new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel()));
        doReturn(null).when(input).getCertificateData();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));

        //then + exception
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullParsedCertificateData() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = spy(new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel()));
        doReturn(null).when(input).getParsedCertificateData();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));

        //then + exception
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullCertificate() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = spy(new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel()));
        doReturn(null).when(input).getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));

        //then + exception
    }

    @Test
    void testImportConstructorShouldThrowExceptionWhenCalledWithNullKeyData() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = spy(new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel()));
        doReturn(null).when(input).getKeyData();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));

        //then + exception
    }

    @Test
    void testImportConstructorShouldSetOriginalCertificateWhenCalledWithValidInput() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));

        final X509CertificateModel x509Properties = new X509CertificateModel();
        x509Properties.setSubject("CN=example.com");
        x509Properties.setSubjectAlternativeNames(new SubjectAlternativeNames(Set.of("*.localhost"), Set.of(), Set.of()));
        x509Properties.setExtendedKeyUsage(Set.of());
        x509Properties.setKeyUsage(Set.of(KeyUsageEnum.ENCIPHER_ONLY));
        x509Properties.setValidityMonths(TWO_YEARS_IN_MONTHS);

        final CertificatePolicyModel policyModel = new CertificatePolicyModel();
        policyModel.setX509Properties(x509Properties);

        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, policyModel);
        final X509Certificate certificate = input.getCertificate();
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity actual = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        Assertions.assertEquals(certificate, actual.getCertificate());
        final ReadOnlyCertificatePolicy originalPolicy = actual.getOriginalCertificatePolicy();
        Assertions.assertEquals("CN=localhost", originalPolicy.getSubject());
        Assertions.assertIterableEquals(
                new TreeSet<>(Set.of("localhost", "127.0.0.1")),
                new TreeSet<>(originalPolicy.getDnsNames()));
        Assertions.assertIterableEquals(Set.of(), originalPolicy.getUpns());
        Assertions.assertIterableEquals(Set.of(), originalPolicy.getEmails());
        Assertions.assertIterableEquals(
                new TreeSet<>(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2")),
                new TreeSet<>(originalPolicy.getExtendedKeyUsage()));
        Assertions.assertIterableEquals(
                new TreeSet<>(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT, KeyUsageEnum.DIGITAL_SIGNATURE)),
                new TreeSet<>(originalPolicy.getKeyUsage()));
        Assertions.assertEquals(THIRTY_YEARS_IN_MONTHS, originalPolicy.getValidityMonths());
        Assertions.assertNotNull(actual.getOriginalCertificateContents());
    }

    @Test
    void testCreateConstructorShouldSetOriginalCertificateWhenCalledWithValidInput() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .name(CERT_NAME_1)
                .enableTransparency(false)
                .certAuthorityType(SELF_SIGNED)
                .contentType(CertContentType.PKCS12)
                .keyCurveName(KeyCurveName.P_521)
                .keyType(KeyType.EC)
                .validityMonths(TWO_YEARS_IN_MONTHS)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity actual = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        final ReadOnlyCertificatePolicy currentPolicy = actual.getIssuancePolicy();
        final ReadOnlyCertificatePolicy originalPolicy = actual.getOriginalCertificatePolicy();
        Assertions.assertEquals(currentPolicy, originalPolicy);
        Assertions.assertNotNull(actual.getOriginalCertificateContents());
    }

    @Test
    void testRegenerateCertificateShouldRegenerateCertificateWhenTheValidityIsNoLongerAccurate() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .name(CERT_NAME_1)
                .enableTransparency(false)
                .certAuthorityType(SELF_SIGNED)
                .contentType(CertContentType.PKCS12)
                .keyCurveName(KeyCurveName.P_521)
                .keyType(KeyType.EC)
                .validityMonths(TWO_YEARS_IN_MONTHS)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final KeyVaultCertificateEntity underTest = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);
        underTest.timeShift((int) Duration.ofDays(1).toSeconds());
        final X509Certificate original = (X509Certificate) underTest.getCertificate();

        //when
        underTest.regenerateCertificate(vault);

        //then
        final X509Certificate actual = (X509Certificate) underTest.getCertificate();
        Assertions.assertNotEquals(original, actual);
    }

    @Test
    void testRegenerateCertificateShouldNotRegenerateCertificateWhenTheValidityIsStillAccurate() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .name(CERT_NAME_1)
                .enableTransparency(false)
                .certAuthorityType(SELF_SIGNED)
                .contentType(CertContentType.PKCS12)
                .keyCurveName(KeyCurveName.P_521)
                .keyType(KeyType.EC)
                .validityMonths(TWO_YEARS_IN_MONTHS)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final KeyVaultCertificateEntity underTest = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);
        underTest.timeShift((int) Duration.ofHours(1).toSeconds());
        final X509Certificate original = (X509Certificate) underTest.getCertificate();

        //when
        underTest.regenerateCertificate(vault);

        //then
        final X509Certificate actual = (X509Certificate) underTest.getCertificate();
        Assertions.assertSame(original, actual);
    }
}
