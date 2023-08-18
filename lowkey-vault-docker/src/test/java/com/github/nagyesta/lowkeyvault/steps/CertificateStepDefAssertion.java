package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.models.*;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.github.nagyesta.lowkeyvault.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.context.CertificateTestContext;
import com.github.nagyesta.lowkeyvault.context.KeyTestContext;
import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.crypto.CryptoException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Base64.getMimeDecoder;

public class CertificateStepDefAssertion extends CommonAssertions {

    private static final String DEFAULT_PASSWORD = "";
    private final CertificateTestContext context;
    private final SecretTestContext secretContext;
    private final KeyTestContext keyContext;

    public CertificateStepDefAssertion(final TestContextConfig config) {
        this.context = config.certificateContext();
        this.secretContext = config.secretContext();
        this.keyContext = config.keyContext();
    }

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

    @And("the downloaded {certContentType} certificate store was shifted {int} days, using renewals {int} days before {int} months expiry")
    public void theDownloadedTypeCertificateStoreWasShiftedDaysUsingMonthsOfExpiry(
            final CertificateContentType contentType, final int daysShifted,
            final int renewalThreshold, final int expiryMonths) throws Exception {
        final String value = secretContext.getLastResult().getValue();
        final X509Certificate certificate = getX509Certificate(contentType, value);
        final OffsetDateTime expiry = calculateExpiry(expiryMonths, daysShifted, renewalThreshold);
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
            //compare PEM content
            assertEquals(expected, value);
            //Check whether private key and public key are a pair
            final PrivateKey actualKey = getKeyFromPem(extractKeyByteArray(value), certificate);
            assertPrivateKeyMatchesPublic(certificate, actualKey);
        } else {
            //compare certificates
            final KeyStore expectedKeyStore = getKeyStore(content, password);
            final String expectedAlias = findAlias(expectedKeyStore);
            final X509Certificate expectedCertificate = (X509Certificate) expectedKeyStore.getCertificate(expectedAlias);
            assertEquals(expectedCertificate, certificate);
            //compare keys
            final Key expectedKey = expectedKeyStore.getKey(expectedAlias, password.toCharArray());
            final KeyStore actualKeyStore = getKeyStore(getMimeDecoder().decode(value), DEFAULT_PASSWORD);
            final String actualAlias = findAlias(actualKeyStore);
            final Key actualKey = actualKeyStore.getKey(actualAlias, DEFAULT_PASSWORD.toCharArray());
            assertKeyEquals(expectedKey, actualKey);
            //Check whether private key and public key are a pair
            assertPrivateKeyMatchesPublic(certificate, (PrivateKey) actualKey);
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

    @And("the lifetime action triggers {certAction} when {int} {certLifetimePercentageTrigger} reached")
    public void theLifetimeActionTriggersActionWhenTriggerValueTriggerTypeReached(
            final CertificatePolicyAction action, final int triggerValue, final boolean isPercentage) {
        final CertificatePolicy policy = context.getClient(context.getCertificateServiceVersion())
                .getCertificatePolicy(context.getLastResult().getName());
        final List<LifetimeAction> actions = policy.getLifetimeActions();
        assertNotNull(actions);
        assertEquals(1, actions.size());
        final LifetimeAction actual = actions.get(0);
        assertEquals(action, actual.getAction());
        if (isPercentage) {
            assertEquals(triggerValue, actual.getLifetimePercentage());
            assertNull(actual.getDaysBeforeExpiry());
        } else {
            assertEquals(triggerValue, actual.getDaysBeforeExpiry());
            assertNull(actual.getLifetimePercentage());
        }
    }

    @And("the downloaded certificate policy has {keyUsage} and {keyUsage} as key usages")
    public void theDownloadedCertificatePolicyHasKeyUsagesAsKeyUsages(
            final CertificateKeyUsage keyUsage1, final CertificateKeyUsage keyUsage2) {
        final List<CertificateKeyUsage> expected = List.of(keyUsage1, keyUsage2);
        final List<CertificateKeyUsage> actual = context.getDownloadedPolicy().getKeyUsage();
        assertEquals(expected.size(), actual.size());
        expected.forEach(k ->
                assertTrue("Key usage not found: " + k + " in list: " + actual, actual.contains(k)));
    }

    @And("the downloaded certificate policy has {enhancedKeyUsage} and {enhancedKeyUsage} as enhanced key usages")
    public void theDownloadedCertificatePolicyHasExtendedKeyUsagesAsKeyUsages(
            final String enhancedKeyUsage1, final String enhancedKeyUsage2) {
        final List<String> expected = List.of(enhancedKeyUsage1, enhancedKeyUsage2);
        final List<String> actual = context.getDownloadedPolicy().getEnhancedKeyUsage();
        assertEquals(expected.size(), actual.size());
        expected.forEach(k ->
                assertTrue("Enhanced key usage not found: " + k + " in list: " + actual, actual.contains(k)));
    }

    @And("the certificate has no tags")
    public void theCertificateHasNoTags() {
        final Map<String, String> tags = context.getLastResult().getProperties().getTags();
        assertTrue("The certificate should have no tags, but has: " + tags, tags.isEmpty());
    }

    @Then("the certificate has a tag named {} with {} as value")
    public void theCertificateHasATagNamedWithValue(final String name, final String value) {
        final Map<String, String> tags = context.getLastResult().getProperties().getTags();
        assertEquals(value, tags.get(name));
        assertTrue("The certificate should have only 1 tag, but has: " + tags, tags.size() == 1);
    }

    @And("the downloaded certificate policy has {certContentType} as type")
    public void theDownloadedCertificatePolicyHasTypeAsType(final CertificateContentType contentType) {
        final CertificatePolicy certificatePolicy = context.getDownloadedPolicy();
        assertEquals(contentType, certificatePolicy.getContentType());
    }

    private static OffsetDateTime calculateExpiry(final int expiryMonths, final int shiftedDays, final int renewalDaysBeforeExpiry) {
        final OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime currentRenewalDate = now.minusDays(shiftedDays);
        while (currentRenewalDate.isBefore(now)) {
            currentRenewalDate = currentRenewalDate.plusMonths(expiryMonths).minusDays(renewalDaysBeforeExpiry);
        }
        return currentRenewalDate.plusDays(renewalDaysBeforeExpiry);
    }

    private PrivateKey getKeyFromPem(final byte[] content, final X509Certificate certificate) throws CryptoException {
        try {
            final KeyFactory kf = KeyFactory.getInstance(certificate.getPublicKey().getAlgorithm(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            final PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(content);
            return kf.generatePrivate(privSpec);
        } catch (final Exception e) {
            throw new CryptoException("Failed to acquire key, sue to exception: " + e.getMessage(), e);
        }
    }

    private void assertPrivateKeyMatchesPublic(final X509Certificate certificate, final PrivateKey actualKey) throws CryptoException {
        final byte[] thumbprint = getThumbprint(certificate);
        final byte[] testSignature = sign(thumbprint, actualKey, certificate.getSigAlgName());
        final boolean verify = verify(thumbprint, testSignature, certificate.getPublicKey(), certificate.getSigAlgName());
        assertTrue("The digest signed with the private key should have been verified using the public key.", verify);
    }

    private byte[] sign(final byte[] digest, final PrivateKey key, final String sigAlgName) throws CryptoException {
        try {
            final Signature signature = Signature.getInstance(sigAlgName);
            signature.initSign(key);
            signature.update(digest);
            return signature.sign();
        } catch (final Exception e) {
            throw new CryptoException("Unable to sign digest using algorithm: " + sigAlgName);
        }
    }

    private boolean verify(final byte[] digest, final byte[] signature, final PublicKey key, final String sigAlgName) throws CryptoException {
        try {
            final Signature verify = Signature.getInstance(sigAlgName);
            verify.initVerify(key);
            verify.update(digest);
            return verify.verify(signature);
        } catch (final Exception e) {
            throw new CryptoException("Unable to sign digest using algorithm: " + sigAlgName);
        }
    }

    public byte[] getThumbprint(final X509Certificate certificate) throws CryptoException {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(certificate.getEncoded());
            return messageDigest.digest();
        } catch (final Exception e) {
            throw new CryptoException("Failed to calculate thumbprint for certificate: " + certificate.getSubjectDN().getName(), e);
        }
    }

    private void assertKeyEquals(final Key expectedKey, final Key actualKey) {
        if (actualKey instanceof ECPrivateKey) {
            assertEcKeyEquals((ECPrivateKey) expectedKey, (ECPrivateKey) actualKey);
        } else if (actualKey instanceof RSAPrivateKey) {
            assertRsaKeyEquals((RSAPrivateKey) expectedKey, (RSAPrivateKey) actualKey);
        } else {
            assertFail("Unknown key type found: expected=" + expectedKey.getClass() + ", actual=" + actualKey.getClass());
        }
    }

    private void assertRsaKeyEquals(final RSAPrivateKey expectedKey, final RSAPrivateKey actualKey) {
        assertArrayEquals(actualKey.getEncoded(), expectedKey.getEncoded());
    }

    private void assertEcKeyEquals(final ECPrivateKey expectedKey, final ECPrivateKey actualKey) {
        assertEquals(expectedKey.getAlgorithm(), actualKey.getAlgorithm());
        assertEquals(expectedKey.getFormat(), actualKey.getFormat());
        assertEquals(expectedKey.getS(), actualKey.getS());
        assertEquals(expectedKey.getParams(), actualKey.getParams());
        assertEquals(expectedKey.getFormat(), actualKey.getFormat());
    }

    private X509Certificate getX509Certificate(final CertificateContentType contentType, final String value) throws Exception {
        final X509Certificate certificate;
        if (contentType == CertificateContentType.PEM) {
            final byte[] encodedCertificate = extractCertificateByteArray(value);
            final CertificateFactory fact = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(encodedCertificate));
        } else {
            final byte[] bytes = getMimeDecoder().decode(value);
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
        final KeyStore keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(new ByteArrayInputStream(content), password.toCharArray());
        return keyStore;
    }

    private byte[] extractCertificateByteArray(final String certificateContent) {
        final String withoutNewLines = certificateContent.replaceAll("[\n\r]+", "");
        final String keyOnly = withoutNewLines.replaceAll(".*" + "-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----" + ".*", "");
        return Base64.decodeBase64(keyOnly);
    }

    private byte[] extractKeyByteArray(final String certificateContent) {
        final String withoutNewLines = certificateContent.replaceAll("[\n\r]+", "");
        final String keyOnly = withoutNewLines.replaceAll(".*" + "-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----" + ".*", "");
        return Base64.decodeBase64(keyOnly);
    }
}
