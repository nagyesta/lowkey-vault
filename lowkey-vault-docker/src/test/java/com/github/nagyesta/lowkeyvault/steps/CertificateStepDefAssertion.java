package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateKeyUsage;
import com.azure.security.keyvault.certificates.models.CertificatePolicyAction;
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
import java.util.List;
import java.util.Objects;

import static java.util.Base64.getDecoder;
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
        final var secret = secretContext.getClient(secretContext.getSecretServiceVersion()).getSecret(name);
        secretContext.addFetchedSecret(name, secret);
        assertTrue(secret.getProperties().isManaged());
        assertNotNull(secret.getProperties().getKeyId());
        final var key = keyContext.getClient(keyContext.getKeyServiceVersion()).getKey(name);
        keyContext.addFetchedKey(name, key);
        assertTrue(key.getProperties().isManaged());
    }

    @And("the downloaded secret contains a {certContentType} certificate")
    public void theDownloadedSecretContainsATypeCertificate(final CertificateContentType contentType) throws Exception {
        final var value = secretContext.getLastResult().getValue();
        final var x509Certificate = getX509Certificate(contentType, value);
        assertNotNull(x509Certificate);
    }

    @And("the downloaded {certContentType} certificate store has a certificate with {subject} as subject")
    public void theDownloadedTypeCertificateStoreHasACertificateWithSubjectAsSubject(
            final CertificateContentType contentType, final String subject) throws Exception {
        final var value = secretContext.getLastResult().getValue();
        final var certificate = getX509Certificate(contentType, value);
        assertEquals(subject, certificate.getSubjectX500Principal().toString());
    }

    @And("the downloaded {certContentType} certificate store expires on {expiry}")
    public void theDownloadedTypeCertificateStoreExpiresOnExpiry(
            final CertificateContentType contentType, final OffsetDateTime expiry) throws Exception {
        final var value = secretContext.getLastResult().getValue();
        final var certificate = getX509Certificate(contentType, value);
        assertEquals(expiry.toInstant().truncatedTo(ChronoUnit.DAYS),
                certificate.getNotAfter().toInstant().truncatedTo(ChronoUnit.DAYS));
    }

    @And("the downloaded {certContentType} certificate store was shifted {int} days, using renewals {int} days before {int} months expiry")
    public void theDownloadedTypeCertificateStoreWasShiftedDaysUsingMonthsOfExpiry(
            final CertificateContentType contentType, final int daysShifted,
            final int renewalThreshold, final int expiryMonths) throws Exception {
        final var value = secretContext.getLastResult().getValue();
        final var certificate = getX509Certificate(contentType, value);
        final var expiry = calculateExpiry(expiryMonths, daysShifted, renewalThreshold);
        assertEquals(expiry.toInstant().truncatedTo(ChronoUnit.DAYS),
                certificate.getNotAfter().toInstant().truncatedTo(ChronoUnit.DAYS));
    }

    @And("the downloaded {certContentType} certificate store content matches store from {fileName} using {password} as password")
    public void theDownloadedTypeCertificateStoreContentMatchesStoreFromFileNameUsingPassword(
            final CertificateContentType contentType, final String resource, final String password) throws Exception {
        final var content = Objects.requireNonNull(getClass().getResourceAsStream("/certs/" + resource)).readAllBytes();
        final var value = secretContext.getLastResult().getValue();
        final var certificate = getX509Certificate(contentType, value);
        if (contentType == CertificateContentType.PEM) {
            final var expected = new String(content, StandardCharsets.UTF_8);
            //compare PEM content
            assertEquals(expected, value);
            //Check whether private key and public key are a pair
            final var actualKey = getKeyFromPem(extractKeyByteArray(value), certificate);
            assertPrivateKeyMatchesPublic(certificate, actualKey);
        } else {
            //compare certificates
            final var expectedKeyStore = getKeyStore(content, password);
            final var expectedAlias = findAlias(expectedKeyStore);
            final var expectedCertificate = (X509Certificate) expectedKeyStore.getCertificate(expectedAlias);
            assertEquals(expectedCertificate, certificate);
            //compare keys
            final var expectedKey = expectedKeyStore.getKey(expectedAlias, password.toCharArray());
            final var actualKeyStore = getKeyStore(getMimeDecoder().decode(value), DEFAULT_PASSWORD);
            final var actualAlias = findAlias(actualKeyStore);
            final var actualKey = actualKeyStore.getKey(actualAlias, DEFAULT_PASSWORD.toCharArray());
            assertKeyEquals(expectedKey, actualKey);
            //Check whether private key and public key are a pair
            assertPrivateKeyMatchesPublic(certificate, (PrivateKey) actualKey);
        }
    }

    @Then("the list of certificates should contain {int} items")
    public void theListShouldContainCountItems(final int count) {
        final var ids = context.getListedIds();
        assertEquals(count, ids.size());
    }

    @Then("the deleted list should contain {int} items")
    public void theDeletedListShouldContainCountItems(final int count) {
        final var ids = context.getDeletedRecoveryIds();
        assertEquals(count, ids.size());
    }

    @And("the downloaded certificate policy has {int} months validity")
    public void theDownloadedCertificatePolicyHasMonthsValidity(final int validity) {
        final var actual = context.getDownloadedPolicy().getValidityInMonths();
        assertEquals(validity, actual);
    }

    @And("the downloaded certificate policy has {subject} as subject")
    public void theDownloadedCertificatePolicyHasSubjectAsSubject(final String subject) {
        final var actual = context.getDownloadedPolicy().getSubject();
        assertEquals(subject, actual);
    }

    @And("the deleted certificate policy named {name} is downloaded")
    public void theDeletedCertificatePolicyNamedMultiImportIsDownloaded(final String name) {
        final var client = context.getClient(context.getCertificateServiceVersion());
        final var deletedCertificate = client.getDeletedCertificate(name);
        context.setLastDeleted(deletedCertificate);
        assertNotNull(deletedCertificate);
    }

    @And("the lifetime action triggers {certAction} when {int} {certLifetimePercentageTrigger} reached")
    public void theLifetimeActionTriggersActionWhenTriggerValueTriggerTypeReached(
            final CertificatePolicyAction action, final int triggerValue, final boolean isPercentage) {
        final var policy = context.getClient(context.getCertificateServiceVersion())
                .getCertificatePolicy(context.getLastResult().getName());
        final var actions = policy.getLifetimeActions();
        assertNotNull(actions);
        assertEquals(1, actions.size());
        final var actual = actions.get(0);
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
        final var expected = List.of(keyUsage1, keyUsage2);
        final var actual = context.getDownloadedPolicy().getKeyUsage();
        assertEquals(expected.size(), actual.size());
        expected.forEach(k ->
                assertTrue("Key usage not found: " + k + " in list: " + actual, actual.contains(k)));
    }

    @And("the downloaded certificate policy has {enhancedKeyUsage} and {enhancedKeyUsage} as enhanced key usages")
    public void theDownloadedCertificatePolicyHasExtendedKeyUsagesAsKeyUsages(
            final String enhancedKeyUsage1, final String enhancedKeyUsage2) {
        final var expected = List.of(enhancedKeyUsage1, enhancedKeyUsage2);
        final var actual = context.getDownloadedPolicy().getEnhancedKeyUsage();
        assertEquals(expected.size(), actual.size());
        expected.forEach(k ->
                assertTrue("Enhanced key usage not found: " + k + " in list: " + actual, actual.contains(k)));
    }

    @And("the certificate has no tags")
    public void theCertificateHasNoTags() {
        final var tags = context.getLastResult().getProperties().getTags();
        assertTrue("The certificate should have no tags, but has: " + tags, tags.isEmpty());
    }

    @Then("the certificate has a tag named {} with {} as value")
    public void theCertificateHasATagNamedWithValue(final String name, final String value) {
        final var tags = context.getLastResult().getProperties().getTags();
        assertEquals(value, tags.get(name));
        assertTrue("The certificate should have only 1 tag, but has: " + tags, tags.size() == 1);
    }

    @And("the downloaded certificate policy has {certContentType} as type")
    public void theDownloadedCertificatePolicyHasTypeAsType(final CertificateContentType contentType) {
        final var certificatePolicy = context.getDownloadedPolicy();
        assertEquals(contentType, certificatePolicy.getContentType());
    }

    private static OffsetDateTime calculateExpiry(final int expiryMonths, final int shiftedDays, final int renewalDaysBeforeExpiry) {
        final var now = OffsetDateTime.now();
        var currentRenewalDate = now.minusDays(shiftedDays);
        while (currentRenewalDate.isBefore(now)) {
            currentRenewalDate = currentRenewalDate.plusMonths(expiryMonths).minusDays(renewalDaysBeforeExpiry);
        }
        return currentRenewalDate.plusDays(renewalDaysBeforeExpiry);
    }

    private PrivateKey getKeyFromPem(final byte[] content, final X509Certificate certificate) throws CryptoException {
        try {
            final var kf = KeyFactory.getInstance(certificate.getPublicKey().getAlgorithm(), KeyGenUtil.BOUNCY_CASTLE_PROVIDER);
            final var privateSpec = new PKCS8EncodedKeySpec(content);
            return kf.generatePrivate(privateSpec);
        } catch (final Exception e) {
            throw new CryptoException("Failed to acquire key, sue to exception: " + e.getMessage(), e);
        }
    }

    private void assertPrivateKeyMatchesPublic(final X509Certificate certificate, final PrivateKey actualKey) throws CryptoException {
        final var thumbprint = getThumbprint(certificate);
        final var testSignature = sign(thumbprint, actualKey, certificate.getSigAlgName());
        final var verify = verify(thumbprint, testSignature, certificate.getPublicKey(), certificate.getSigAlgName());
        assertTrue("The digest signed with the private key should have been verified using the public key.", verify);
    }

    private byte[] sign(final byte[] digest, final PrivateKey key, final String sigAlgName) throws CryptoException {
        try {
            final var signature = Signature.getInstance(sigAlgName);
            signature.initSign(key);
            signature.update(digest);
            return signature.sign();
        } catch (final Exception e) {
            throw new CryptoException("Unable to sign digest using algorithm: " + sigAlgName);
        }
    }

    private boolean verify(final byte[] digest, final byte[] signature, final PublicKey key, final String sigAlgName) throws CryptoException {
        try {
            final var verify = Signature.getInstance(sigAlgName);
            verify.initVerify(key);
            verify.update(digest);
            return verify.verify(signature);
        } catch (final Exception e) {
            throw new CryptoException("Unable to sign digest using algorithm: " + sigAlgName);
        }
    }

    public byte[] getThumbprint(final X509Certificate certificate) throws CryptoException {
        try {
            final var messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(certificate.getEncoded());
            return messageDigest.digest();
        } catch (final Exception e) {
            throw new CryptoException("Failed to calculate thumbprint for certificate: "
                    + certificate.getSubjectX500Principal().getName(), e);
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
            final var encodedCertificate = extractCertificateByteArray(value);
            final var fact = CertificateFactory.getInstance("X.509");
            certificate = (X509Certificate) fact.generateCertificate(new ByteArrayInputStream(encodedCertificate));
        } else {
            final var bytes = getDecoder().decode(value);
            final var keyStore = getKeyStore(bytes, DEFAULT_PASSWORD);
            final var alias = findAlias(keyStore);
            certificate = (X509Certificate) keyStore.getCertificate(alias);
            assertNotNull(keyStore.getKey(alias, DEFAULT_PASSWORD.toCharArray()));
        }
        return certificate;
    }

    private static String findAlias(final KeyStore keyStore) throws KeyStoreException {
        final var aliases = keyStore.aliases();
        return aliases.nextElement();
    }

    private static KeyStore getKeyStore(final byte[] content, final String password)
            throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        final var keyStore = KeyStore.getInstance("pkcs12");
        keyStore.load(new ByteArrayInputStream(content), password.toCharArray());
        return keyStore;
    }

    private byte[] extractCertificateByteArray(final String certificateContent) {
        final var withoutNewLines = certificateContent.replaceAll("[\n\r]+", "");
        final var keyOnly = withoutNewLines.replaceAll(".*" + "-----BEGIN CERTIFICATE-----", "")
                .replaceAll("-----END CERTIFICATE-----" + ".*", "");
        return Base64.decodeBase64(keyOnly);
    }

    private byte[] extractKeyByteArray(final String certificateContent) {
        final var withoutNewLines = certificateContent.replaceAll("[\n\r]+", "");
        final var keyOnly = withoutNewLines.replaceAll(".*" + "-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----" + ".*", "");
        return Base64.decodeBase64(keyOnly);
    }
}
