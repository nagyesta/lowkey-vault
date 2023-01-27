package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.models.*;
import com.github.nagyesta.lowkeyvault.context.CertificateTestContext;
import com.github.nagyesta.lowkeyvault.context.KeyTestContext;
import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.CONTAINER_AUTHORITY;

public class CertificatesStepDefs extends CommonAssertions {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private CertificateTestContext context;
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private SecretTestContext secretContext;
    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private KeyTestContext keyContext;

    @Given("certificate API version {api} is used")
    public void apiVersionApiIsUsed(final String version) {
        context.setApiVersion(version);
        secretContext.setApiVersion(version);
        keyContext.setApiVersion(version);
    }

    @Given("a certificate client is created with the vault named {name}")
    public void theCertificateClientIsCreatedWithVaultNameSelected(final String vaultName) {
        final String vaultAuthority = vaultName + ".localhost:8443";
        final String vaultUrl = "https://" + vaultAuthority;
        final AuthorityOverrideFunction overrideFunction = new AuthorityOverrideFunction(vaultAuthority, CONTAINER_AUTHORITY);
        context.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
        secretContext.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
        keyContext.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
    }

    @Given("a {certContentType} certificate is prepared with subject {subject}")
    public void aCertificateIsPreparedWithNameSubjectSet(
            final CertificateContentType type, final String subject) {
        final CertificatePolicy policy = new CertificatePolicy("Self", subject);
        policy.setContentType(type);
        context.setPolicy(policy);
    }

    @Given("a {certContentType} certificate is prepared with subject {subject} and SANS {sans}")
    public void aCertificateIsPreparedWithNameSubjectAndSanSet(
            final CertificateContentType type, final String subject, final SubjectAlternativeNames sans) {
        final CertificatePolicy policy = new CertificatePolicy("Self", subject, sans);
        policy.setContentType(type);
        context.setPolicy(policy);
    }

    @Given("the certificate is set to use an EC key with {ecCertCurveName} and {hsm} HSM")
    public void theCertificateIsSetToUseAnEcKeyWithKeyCurveAndHsmSet(final CertificateKeyCurveName curveName, final boolean hsm) {
        final CertificatePolicy policy = context.getPolicy();
        policy.setKeyCurveName(curveName);
        if (hsm) {
            policy.setKeyType(CertificateKeyType.EC_HSM);
        } else {
            policy.setKeyType(CertificateKeyType.EC);
        }
        policy.setKeySize(null);
        context.setPolicy(policy);
    }

    @Given("the certificate is set to use an RSA key with {rsaKeySize} and {hsm} HSM")
    public void theCertificateIsSetToUseAnRsaKeyWithKeySizeAndHsmSet(final int keySize, final boolean hsm) {
        final CertificatePolicy policy = context.getPolicy();
        policy.setKeySize(keySize);
        if (hsm) {
            policy.setKeyType(CertificateKeyType.RSA_HSM);
        } else {
            policy.setKeyType(CertificateKeyType.RSA);
        }
        policy.setKeyCurveName(null);
        context.setPolicy(policy);
    }

    @And("the certificate is created with name {name}")
    public void rsaCertificateCreationRequestIsSentWithName(final String name) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        client.beginCreateCertificate(name, context.getPolicy(), true, context.getTags())
                .waitForCompletion();
        final KeyVaultCertificateWithPolicy certificate = client.getCertificate(name);
        context.addCreatedEntity(name, certificate);
    }

    @When("the first certificate version of {name} is fetched with providing a version")
    public void fetchFirstCertificateVersion(final String name) {
        final List<KeyVaultCertificate> versionsCreated = context.getCreatedEntities().get(name);
        final String version = versionsCreated.get(0).getProperties().getVersion();
        final KeyVaultCertificate certificate = context.getClient(context.getCertificateServiceVersion())
                .getCertificateVersion(name, version);
        context.addFetchedCertificate(name, certificate);
        assertEquals(version, certificate.getProperties().getVersion());
    }

    @When("the last certificate version of {name} is fetched without providing a version")
    public void fetchLatestKeyVersion(final String name) {
        final KeyVaultCertificate certificate = context.getClient(context.getCertificateServiceVersion())
                .getCertificateVersion(name, null);
        context.addFetchedCertificate(name, certificate);
    }

    @Given("the certificate is set to be {enabled}")
    public void theKeyIsSetToBeEnabledStatus(final boolean enabledStatus) {
        context.getPolicy().setEnabled(enabledStatus);
    }

    @When("a certificate named {name} is imported from the resource named {fileName} using {password} as password")
    public void aCertificateIsImportedWithNameFromTheResourceUsingPassword(
            final String name, final String resource, final String password) throws IOException {
        final byte[] content = Objects.requireNonNull(getClass().getResourceAsStream("/certs/" + resource)).readAllBytes();
        final ImportCertificateOptions options = new ImportCertificateOptions(name, content);
        Optional.ofNullable(password).ifPresent(options::setPassword);
        final KeyVaultCertificateWithPolicy certificate = context
                .getClient(context.getCertificateServiceVersion())
                .importCertificate(options);
        context.addCreatedEntity(name, certificate);
    }
}
