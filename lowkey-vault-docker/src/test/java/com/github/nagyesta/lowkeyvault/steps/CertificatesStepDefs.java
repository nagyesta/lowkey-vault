package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.models.*;
import com.github.nagyesta.lowkeyvault.context.CertificateTestContext;
import com.github.nagyesta.lowkeyvault.context.KeyTestContext;
import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.CONTAINER_AUTHORITY;

public class CertificatesStepDefs extends CommonAssertions {

    public static final int DEFAULT_LIFETIME_PERCENTAGE = 80;
    private final CertificateTestContext context;
    private final SecretTestContext secretContext;
    private final KeyTestContext keyContext;

    public CertificatesStepDefs(final TestContextConfig config) {
        this.context = config.certificateContext();
        this.secretContext = config.secretContext();
        this.keyContext = config.keyContext();
    }

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
        policy.setKeyUsage(CertificateKeyUsage.DIGITAL_SIGNATURE);
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
    public void certificateCreationRequestIsSentWithName(final String name) {
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
    public void fetchLatestCertificateVersion(final String name) {
        final KeyVaultCertificate certificate = context.getClient(context.getCertificateServiceVersion())
                .getCertificateVersion(name, null);
        context.addFetchedCertificate(name, certificate);
    }

    @Given("the certificate is set to be {enabled}")
    public void theCertificateIsSetToBeEnabledStatus(final boolean enabledStatus) {
        context.getPolicy().setEnabled(enabledStatus);
    }

    @Given("the certificate is set to expire in {int} months")
    public void theCertificateIsSetToBeEnabledStatus(final int expiryMonths) {
        context.getPolicy().setValidityInMonths(expiryMonths);
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

    @When("a certificate named {name} is imported from the resource named {fileName} covering {subject} {isUsing} a lifetime action")
    public void aCertificateIsImportedWithNameFromTheResourceWithLifetimeAction(
            final String name, final String resource, final String subject, final boolean using) throws IOException {
        final byte[] content = Objects.requireNonNull(getClass().getResourceAsStream("/certs/" + resource)).readAllBytes();
        final ImportCertificateOptions options = new ImportCertificateOptions(name, content);
        final CertificatePolicy policy = new CertificatePolicy(name, subject);
        if (using) {
            final LifetimeAction lifetimeAction = new LifetimeAction(CertificatePolicyAction.EMAIL_CONTACTS);
            lifetimeAction.setLifetimePercentage(DEFAULT_LIFETIME_PERCENTAGE);
            policy.setLifetimeActions(lifetimeAction);
        }
        options.setPolicy(policy);
        final KeyVaultCertificateWithPolicy certificate = context
                .getClient(context.getCertificateServiceVersion())
                .importCertificate(options);
        context.addCreatedEntity(name, certificate);
    }

    @When("the certificate policy named {name} is downloaded")
    public void theCertificatePolicyNamedCertNameIsDownloaded(final String name) {
        final CertificatePolicy policy = context
                .getClient(context.getCertificateServiceVersion())
                .getCertificatePolicy(name);
        context.setDownloadedPolicy(policy);
    }

    @And("{int} certificates are imported from the resource named {fileName} using {password} as password")
    public void countCertificatesAreImportedFromTheGivenResourceUsingPassword(
            final int count, final String resource, final String password) throws IOException {
        final byte[] content = Objects.requireNonNull(getClass().getResourceAsStream("/certs/" + resource)).readAllBytes();
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        IntStream.range(1, count + 1).forEach(i -> {
            final String name = "multi-import-" + i;
            final ImportCertificateOptions options = new ImportCertificateOptions(name, content);
            options.setEnabled(true);
            Optional.ofNullable(password).ifPresent(options::setPassword);
            final KeyVaultCertificateWithPolicy certificate = client
                    .importCertificate(options);
            context.addCreatedEntity(name, certificate);
        });
    }

    @When("the certificates are listed")
    public void theCertificatesAreListed() {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        final List<String> actual = client.listPropertiesOfCertificates()
                .mapPage(CertificateProperties::getId)
                .stream().collect(Collectors.toList());
        context.setListedIds(actual);
    }

    @When("the certificate versions are listed")
    public void theCertificateVersionsAreListed() {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        final List<String> actual = client.listPropertiesOfCertificateVersions(context.getLastResult().getName())
                .mapPage(CertificateProperties::getId)
                .stream().collect(Collectors.toList());
        context.setListedIds(actual);
    }

    @And("{int} certificates with {name} prefix are deleted")
    public void certificatesWithMultiImportPrefixAreDeleted(final int count, final String prefix) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        IntStream.range(1, count + 1).forEach(i -> {
            final DeletedCertificate deletedCertificate = client.beginDeleteCertificate(prefix + i)
                    .waitForCompletion().getValue();
            context.setLastDeleted(deletedCertificate);
        });
    }

    @And("{int} certificates with {name} prefix are purged")
    public void certificatesWithMultiImportPrefixArePurged(final int count, final String prefix) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        IntStream.range(1, count + 1).forEach(i -> client
                .purgeDeletedCertificate(prefix + i));
    }

    @And("{int} certificates with {name} prefix are recovered")
    public void certificatesWithMultiImportPrefixAreRecovered(final int count, final String prefix) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        IntStream.range(1, count + 1).forEach(i -> client
                .beginRecoverDeletedCertificate(prefix + i).waitForCompletion());
    }

    @When("the deleted certificates are listed")
    public void theDeletedCertificatesAreListed() {
        final List<String> recoveryIds = context.getClient(context.getCertificateServiceVersion())
                .listDeletedCertificates()
                .stream()
                .map(DeletedCertificate::getRecoveryId)
                .collect(Collectors.toList());
        context.setDeletedRecoveryIds(recoveryIds);
    }

    @And("the lifetime action trigger is set to {certAction} when {int} {certLifetimePercentageTrigger} reached")
    public void theLifetimeActionTriggerIsSetToActionWhenTriggerValueTriggerTypeReached(
            final CertificatePolicyAction action, final int triggerValue, final boolean isPercentage) {
        final CertificatePolicy policy = context.getPolicy();
        final LifetimeAction lifetimeAction = new LifetimeAction(action);
        if (isPercentage) {
            lifetimeAction.setLifetimePercentage(triggerValue);
        } else {
            lifetimeAction.setDaysBeforeExpiry(triggerValue);
        }
        policy.setLifetimeActions(lifetimeAction);
        context.setPolicy(policy);
    }

    @When("the certificate named {name} is updated to have {subject} as subject and {certContentType} as type")
    public void theCertificateNamedCertNameIsUpdatedToHaveUpdatedSubjectAsSubjectAndUpdatedTypeAsType(
            final String name, final String subject, final CertificateContentType contentType) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        final CertificatePolicy policy = client.getCertificatePolicy(name).setContentType(contentType).setSubject(subject);
        final CertificatePolicy updated = client.updateCertificatePolicy(name, policy);
        context.setPolicy(updated);
    }

    @And("the certificate named {name} is updated to contain the tag named {} with {} as value")
    public void theCertificateNamedCertNameIsUpdatedToContainTheTagWithValue(
            final String name, final String tagName, final String tagValue) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        final CertificateProperties properties = client.getCertificate(name).getProperties();
        properties.setTags(Map.of(tagName, tagValue));
        client.updateCertificateProperties(properties);
        final KeyVaultCertificateWithPolicy certificate = client.getCertificate(name);
        context.addFetchedCertificate(name, certificate);
    }

    @And("the certificate named {name} is backed up")
    public void theCertificateWithNameIsBackedUp(final String name) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        final byte[] backup = client.backupCertificate(name);
        context.setBackupBytes(name, backup);
    }

    @And("the certificate named {name} is restored")
    public void theCertificateWithNamedIsRestored(final String name) {
        final CertificateClient client = context.getClient(context.getCertificateServiceVersion());
        final byte[] backup = context.getBackupBytes(name);
        final KeyVaultCertificateWithPolicy certificate = client.restoreCertificateBackup(backup);
        context.addCreatedEntity(name, certificate);
    }
}
