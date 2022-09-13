package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.certificates.models.CertificateContentType;
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
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

public class CertificateStepDefAssertion extends CommonAssertions {

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

    private X509Certificate getX509Certificate(final CertificateContentType contentType, final String value) throws Exception {
        final X509Certificate certificate;
        if (contentType == CertificateContentType.PEM) {
            final byte[] encodedCertificate = extractByteArray(value);
            final CertificateFactory fact = CertificateFactory.getInstance("X.509", KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            certificate = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(encodedCertificate));
        } else {
            final byte[] bytes = Base64Utils.decodeFromString(value);
            final KeyStore keyStore = KeyStore.getInstance("pkcs12", KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            keyStore.load(new ByteArrayInputStream(bytes), "lowkey-vault".toCharArray());
            final Enumeration<String> aliases = keyStore.aliases();
            final String alias = aliases.nextElement();
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            assertNotNull(keyStore.getKey(alias, "lowkey-vault".toCharArray()));
        }
        return certificate;
    }

    private byte[] extractByteArray(final String certificateContent) {
        final String withoutNewLines = certificateContent.replaceAll("[\n\r]+", "");
        final String keyOnly = withoutNewLines.replaceAll(".*" + "-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----" + ".*", "");
        return Base64.decodeBase64(keyOnly);
    }
}
