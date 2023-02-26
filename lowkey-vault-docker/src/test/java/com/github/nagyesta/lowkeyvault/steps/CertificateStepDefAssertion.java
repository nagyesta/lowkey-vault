package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.github.nagyesta.lowkeyvault.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.context.CertificateTestContext;
import com.github.nagyesta.lowkeyvault.context.KeyTestContext;
import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Base64Utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class CertificateStepDefAssertion extends CommonAssertions {

    private static final String DEFAULT_PASSWORD = "lowkey-vault";
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private CertificateTestContext context;
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private SecretTestContext secretContext;
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private KeyTestContext keyContext;

    @Then("the certificate is {enabled}")
    public void theCertificateIsEnabledStatus(final boolean enabled) {
        assertEquals(enabled, context.getLastResult().getProperties().isEnabled());
    }

    @And("the certificate secret named {name} is downloaded")
    public void theCertificateSecretIsDownloaded(final String name) {
        final KeyVaultSecret secret = secretContext.getClient(secretContext.getSecretServiceVersion()).getSecret(name);
        secretContext.addFetchedSecret(name, secret);
        assertTrue(secret.getProperties().isManaged());
        assertNotNull(secret.getProperties().getKeyId());
        final KeyVaultKey key = keyContext.getClient(keyContext.getKeyServiceVersion()).getKey(name);
        keyContext.addFetchedKey(name, key);
        assertTrue(key.getProperties().isManaged());
    }

    @And("the downloaded secret contains a {certContentType} certificate")
    public void theDownloadedSecretContainsATypeCertificate(final CertificateContentType contentType) throws Exception {
        final String value = secretContext.getLastResult().getValue();
        final X509Certificate x509Certificate = getX509Certificate(contentType, value);
        assertNotNull(x509Certificate);
    }

    @And("the downloaded {certContentType} certificate store has a certificate with {subject} as subject")
    public void theDownloadedTypeCertificateStoreHasACertificateWithSubjectAsSubject(
            final CertificateContentType contentType, final String subject) throws Exception {
        final String value = secretContext.getLastResult().getValue();
        final X509Certificate certificate = getX509Certificate(contentType, value);
        assertEquals(subject, certificate.getSubjectX500Principal().toString());
    }

    @And("the downloaded {certContentType} certificate store expires on {expiry}")
    public void theDownloadedTypeCertificateStoreExpiresOnExpiry(
            final CertificateContentType contentType, final OffsetDateTime expiry) throws Exception {
        final String value = secretContext.getLastResult().getValue();
        final X509Certificate certificate = getX509Certificate(contentType, value);
        assertEquals(expiry.toInstant().truncatedTo(ChronoUnit.DAYS),
                certificate.getNotAfter().toInstant().truncatedTo(ChronoUnit.DAYS));
    }

    @And("the downloaded {certContentType} certificate store content matches store from {fileName} using {password} as password")
    public void theDownloadedTypeCertificateStoreContentMatchesStoreFromFileNameUsingPassword(
            final CertificateContentType contentType, final String resource, final String password) throws Exception {
        final byte[] content = Objects.requireNonNull(getClass().getResourceAsStream("/certs/" + resource)).readAllBytes();
        final String value = secretContext.getLastResult().getValue();
        final X509Certificate certificate = getX509Certificate(contentType, value);
        if (contentType == CertificateContentType.PEM) {
            final String expected = new String(content, StandardCharsets.UTF_8);
            assertEquals(expected, value);
        } else {
            final KeyStore keyStore = getKeyStore(content, password);
            final String alias = findAlias(keyStore);
            final X509Certificate expectedCertificate = (X509Certificate) keyStore.getCertificate(alias);
            assertEquals(expectedCertificate, certificate);
            final Key key = keyStore.getKey(alias, password.toCharArray());
            final KeyStore expectedKeyStore = getKeyStore(Base64Utils.decodeFromString(value), DEFAULT_PASSWORD);
            final String expectedAlias = findAlias(expectedKeyStore);
            final Key expectedKey = expectedKeyStore.getKey(expectedAlias, DEFAULT_PASSWORD.toCharArray());
            assertEquals(expectedKey, key);
        }
    }

    @Then("the list should contain {int} items")
    public void theListShouldContainCountItems(final int count) {
        final List<String> ids = context.getListedIds();
        assertEquals(count, ids.size());
    }

    @Then("the deleted list should contain {int} items")
    public void theDeletedListShouldContainCountItems(final int count) {
        final List<String> ids = context.getDeletedRecoveryIds();
        assertEquals(count, ids.size());
    }

    @And("the downloaded certificate policy has {int} months validity")
    public void theDownloadedCertificatePolicyHasMonthsValidity(final int validity) {
        final Integer actual = context.getDownloadedPolicy().getValidityInMonths();
        assertEquals(validity, actual);
    }

    @And("the downloaded certificate policy has {subject} as subject")
    public void theDownloadedCertificatePolicyHasSubjectAsSubject(final String subject) {
        final String actual = context.getDownloadedPolicy().getSubject();
        assertEquals(subject, actual);
    }

    @And("the deleted certificate policy named {name} is downloaded")
    public void theDeletedCertificatePolicyNamedMultiImportIsDownloaded(final String name) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        final DeletedCertificate deletedCertificate = client.getDeletedCertificate(name);
        context.setLastDeleted(deletedCertificate);
        assertNotNull(deletedCertificate);
    }

    private X509Certificate getX509Certificate(final CertificateContentType contentType, final String value) throws Exception {
        final X509Certificate certificate;
        if (contentType == CertificateContentType.PEM) {
            final byte[] encodedCertificate = extractByteArray(value);
            final CertificateFactory fact = CertificateFactory.getInstance("X.509", KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            certificate = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(encodedCertificate));
        } else {
            final byte[] bytes = Base64Utils.decodeFromString(value);
            final KeyStore keyStore = getKeyStore(bytes, DEFAULT_PASSWORD);
            final String alias = findAlias(keyStore);
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            assertNotNull(keyStore.getKey(alias, DEFAULT_PASSWORD.toCharArray()));
        }
        return certificate;
    }

    private static String findAlias(final KeyStore keyStore) throws KeyStoreException {
        final Enumeration<String> aliases = keyStore.aliases();
        return aliases.nextElement();
    }

    private static KeyStore getKeyStore(final byte[] content, final String password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final KeyStore keyStore = KeyStore.getInstance("pkcs12", KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
        keyStore.load(new ByteArrayInputStream(content), password.toCharArray());
        return keyStore;
    }

    private byte[] extractByteArray(final String certificateContent) {
        final String withoutNewLines = certificateContent.replaceAll("[\n\r]+", "");
        final String keyOnly = withoutNewLines.replaceAll(".*" + "-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----" + ".*", "");
        return Base64.decodeBase64(keyOnly);
    }
}
