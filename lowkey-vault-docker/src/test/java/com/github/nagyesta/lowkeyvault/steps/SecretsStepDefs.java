package com.github.nagyesta.lowkeyvault.steps;

import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;

import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.github.nagyesta.lowkeyvault.context.KeyTestContext.NOW;
import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.CONTAINER_AUTHORITY;
import static java.lang.Boolean.TRUE;

public class SecretsStepDefs extends CommonAssertions {

    private final SecretTestContext context;

    public SecretsStepDefs(final TestContextConfig config) {
        context = config.secretContext();
    }

    @Given("secret API version {api} is used")
    public void apiVersionApiIsUsed(final String version) {
        context.setApiVersion(version);
    }

    @Given("a secret client is created with the vault named {name}")
    public void theSecretClientIsCreatedWithVaultNameSelected(final String vaultName) {
        final var vaultAuthority = vaultName + ".localhost:8443";
        final var vaultUrl = "https://" + vaultAuthority;
        final var overrideFunction = new AuthorityOverrideFunction(vaultAuthority, CONTAINER_AUTHORITY);
        context.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
    }

    @Given("a secret named {name} and valued {secretValue} is prepared")
    public void aSecretNamedSecretNameIsPreparedWithValueSet(
            final String secretName, final String secretValue) {
        context.setCreateSecretOptions(new KeyVaultSecret(secretName, secretValue));
    }

    @Given("{int} version of the secret is created")
    public void versionsCountVersionOfTheSecretIsCreated(final int versionsCount) {
        final var secretCreateInfo = context.getCreateSecretOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final var secret = context.getClient(context.getSecretServiceVersion()).setSecret(secretCreateInfo);
            context.addCreatedEntity(secretCreateInfo.getName(), secret);
        });
    }

    @When("the secret is created")
    public void secretCreationRequestIsSent() {
        final var secretCreateInfo = context.getCreateSecretOptions();
        final var secret = context.getClient(context.getSecretServiceVersion()).setSecret(secretCreateInfo);
        context.addCreatedEntity(secretCreateInfo.getName(), secret);
    }

    @When("the first secret version of {name} is fetched with providing a version")
    public void fetchFirstSecretVersion(final String name) {
        final var versionsCreated = context.getCreatedEntities().get(name);
        final var version = versionsCreated.getFirst().getProperties().getVersion();
        final var secret = context.getClient(context.getSecretServiceVersion()).getSecret(name, version);
        context.addFetchedSecret(name, secret);
        assertEquals(version, secret.getProperties().getVersion());
    }

    @When("the last secret version of {name} is fetched without providing a version")
    public void fetchLatestSecretVersion(final String name) {
        final var secret = context.getClient(context.getSecretServiceVersion()).getSecret(name);
        final var versionsCreated = context.getCreatedEntities().get(name);
        final var expectedLastVersionId = versionsCreated.getLast().getId();
        context.addFetchedSecret(name, secret);
        assertEquals(expectedLastVersionId, secret.getId());
    }

    @Given("the secret is set to expire {optionalInt} seconds after creation")
    public void theSecretIsSetToExpireExpiresSecondsAfterCreation(final Integer expire) {
        Optional.ofNullable(expire).ifPresent(e -> context.getCreateSecretOptions().getProperties().setExpiresOn(NOW.plusSeconds(e)));
    }

    @Given("the secret is set to be not usable until {optionalInt} seconds after creation")
    public void theSecretIsSetToBeNotUsableUntilNotBeforeSecondsAfterCreation(final Integer notBefore) {
        Optional.ofNullable(notBefore).ifPresent(n -> context.getCreateSecretOptions().getProperties().setNotBefore(NOW.plusSeconds(n)));
    }

    @Given("the secret is set to use {tagMap} as tags")
    public void theSecretIsSetToUseTagMapAsTags(final Map<String, String> tags) {
        context.getCreateSecretOptions().getProperties().setTags(tags);
    }

    @Given("the secret is set to have {contentType} as content type")
    public void theSecretIsSetToHaveContentTypeAsContentType(final String contentType) {
        context.getCreateSecretOptions().getProperties().setContentType(contentType);
    }

    @Given("the secret is set to be {enabled}")
    public void theSecretIsSetToBeEnabledStatus(final boolean enabledStatus) {
        context.getCreateSecretOptions().getProperties().setEnabled(enabledStatus);
    }

    @And("the secret is deleted")
    public void theSecretIsDeleted() {
        final var actual = context.getClient(context.getSecretServiceVersion())
                .beginDeleteSecret(context.getLastResult().getName()).waitForCompletion().getValue();
        context.setLastDeleted(actual);
    }

    @Given("{int} secrets with {name} prefix are created valued {secretValue}")
    public void secretsWithSecretNamePrefixAreCreatedWithValueSet(
            final int count, final String prefix, final String value) {
        IntStream.range(0, count).forEach(i -> {
            aSecretNamedSecretNameIsPreparedWithValueSet(prefix + (i + 1), value);
            secretCreationRequestIsSent();
        });
    }

    @When("the secret properties are listed")
    public void theSecretPropertiesAreListed() {
        final var actual = context.getClient(context.getSecretServiceVersion()).listPropertiesOfSecrets();
        final var propertyList = actual.stream()
                .toList();
        final var list = propertyList.stream()
                .map(SecretProperties::getId)
                .toList();
        context.setListedIds(list);
        final var managedList = propertyList.stream()
                .filter(secretProperties -> TRUE == secretProperties.isManaged())
                .map(SecretProperties::getId)
                .toList();
        context.setListedManagedIds(managedList);
    }

    @Given("{int} secrets with {name} prefix are deleted")
    public void countSecretsWithKeyNamePrefixAreDeleted(
            final int count, final String prefix) {
        final var deleted = IntStream.range(0, count).mapToObj(i -> {
            final var actual = context.getClient(context.getSecretServiceVersion())
                    .beginDeleteSecret(prefix + (i + 1)).waitForCompletion().getValue();
            context.setLastDeleted(actual);
            return actual;
        }).map(DeletedSecret::getRecoveryId).toList();
        context.setDeletedRecoveryIds(deleted);
    }

    @When("the deleted secret properties are listed")
    public void theDeletedSecretPropertiesAreListed() {
        final var actual = context.getClient(context.getSecretServiceVersion()).listDeletedSecrets();
        final var list = actual.stream()
                .map(DeletedSecret::getRecoveryId)
                .toList();
        context.setListedIds(list);
    }

    @When("secret is recovered")
    public void secretIsRecovered() {
        final var deleted = context.getLastDeleted();
        final var secret = context.getClient(context.getSecretServiceVersion())
                .beginRecoverDeletedSecret(deleted.getName()).waitForCompletion().getValue();
        context.addFetchedSecret(secret.getName(), secret);
    }

    @When("the secret is purged")
    public void theSecretIsPurged() {
        final var deleted = context.getLastDeleted();
        context.getClient(context.getSecretServiceVersion()).purgeDeletedSecret(deleted.getName());
    }

    @When("the last version of the secret is prepared for an update")
    public void theLastVersionOfTheSecretIsPreparedForAnUpdate() {
        final var lastResult = context.getLastResult();
        final var updatedProperties = context.getClient(context.getSecretServiceVersion())
                .getSecret(lastResult.getName(), lastResult.getProperties().getVersion()).getProperties();
        context.setUpdateProperties(updatedProperties);
    }

    @When("the secret is updated to expire {optionalInt} seconds after creation")
    public void theSecretIsUpdatedToExpireExpiresSecondsAfterCreation(final Integer expire) {
        Optional.ofNullable(expire).ifPresent(e -> context.getUpdateProperties().setExpiresOn(NOW.plusSeconds(e)));
    }

    @When("the secret is updated to be not usable until {optionalInt} seconds after creation")
    public void theSecretIsUpdatedToBeNotUsableUntilNotBeforeSecondsAfterCreation(final Integer notBefore) {
        Optional.ofNullable(notBefore).ifPresent(n -> context.getUpdateProperties().setNotBefore(NOW.plusSeconds(n)));
    }

    @When("the secret is updated to use {tagMap} as tags")
    public void theSecretIsUpdatedToUseTagMapAsTags(final Map<String, String> tags) {
        context.getUpdateProperties().setTags(tags);
    }

    @When("the secret is updated to be {enabled}")
    public void theSecretIsUpdatedToBeEnabledStatus(final boolean enabledStatus) {
        context.getUpdateProperties().setEnabled(enabledStatus);
    }

    @When("the secret update request is sent")
    public void theUpdateRequestIsSent() {
        final var properties = context.getClient(context.getSecretServiceVersion())
                .updateSecretProperties(context.getUpdateProperties());
        //only update properties as the secret might be disabled due to our actions
        context.getLastResult().setProperties(properties);
    }

    @And("the secret named {name} is backed up")
    public void theSecretNamedNameIsBackedUp(final String name) {
        final var bytes = context.getClient(context.getSecretServiceVersion()).backupSecret(name);
        context.setBackupBytes(name, bytes);
    }

    @And("the secret named {name} is restored")
    public void theSecretNamedNameIsRestored(final String name) {
        final var bytes = context.getBackupBytes(name);
        final var secret = context.getClient(context.getSecretServiceVersion()).restoreSecretBackup(bytes);
        context.addFetchedSecret(name, secret);
    }
}
