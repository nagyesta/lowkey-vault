package com.github.nagyesta.lowkeyvault.steps;

import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.nagyesta.lowkeyvault.context.KeyTestContext.NOW;

public class SecretsStepDefs extends CommonAssertions {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private SecretTestContext context;

    @Given("a secret client is created with the vault named {name}")
    public void theSecretClientIsCreatedWithVaultNameSelected(final String vaultName) {
        context.setProvider(new ApacheHttpClientProvider("https://" + vaultName + ".localhost:8443"));
    }

    @Given("a secret named {name} and valued {secretValue} is prepared")
    public void aSecretNamedSecretNameIsPreparedWithValueSet(
            final String secretName, final String secretValue) {
        context.setCreateSecretOptions(new KeyVaultSecret(secretName, secretValue));
    }

    @Given("{int} version of the secret is created")
    public void versionsCountVersionOfTheSecretIsCreated(final int versionsCount) {
        final KeyVaultSecret secretCreateInfo = context.getCreateSecretOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final KeyVaultSecret secret = context.getClient().setSecret(secretCreateInfo);
            context.addCreatedEntity(secretCreateInfo.getName(), secret);
        });
    }

    @When("the secret is created")
    public void secretCreationRequestIsSent() {
        final KeyVaultSecret secretCreateInfo = context.getCreateSecretOptions();
        final KeyVaultSecret secret = context.getClient().setSecret(secretCreateInfo);
        context.addCreatedEntity(secretCreateInfo.getName(), secret);
    }

    @When("the first secret version of {name} is fetched with providing a version")
    public void fetchFirstSecretVersion(final String name) {
        final List<KeyVaultSecret> versionsCreated = context.getCreatedEntities().get(name);
        final String version = versionsCreated.get(0).getProperties().getVersion();
        final KeyVaultSecret secret = context.getClient().getSecret(name, version);
        context.addFetchedSecret(name, secret);
        assertEquals(version, secret.getProperties().getVersion());
    }

    @When("the last secret version of {name} is fetched without providing a version")
    public void fetchLatestSecretVersion(final String name) {
        final KeyVaultSecret secret = context.getClient().getSecret(name);
        final List<KeyVaultSecret> versionsCreated = context.getCreatedEntities().get(name);
        final String expectedLastVersionId = versionsCreated.get(versionsCreated.size() - 1).getId();
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
        final DeletedSecret actual = context.getClient()
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
        final PagedIterable<SecretProperties> actual = context.getClient().listPropertiesOfSecrets();
        final List<String> list = actual.stream()
                .map(SecretProperties::getId)
                .collect(Collectors.toList());
        context.setListedIds(list);
    }

    @Given("{int} secrets with {name} prefix are deleted")
    public void countSecretsWithKeyNamePrefixAreDeleted(
            final int count, final String prefix) {
        final List<String> deleted = IntStream.range(0, count).mapToObj(i -> {
            final DeletedSecret actual = context.getClient().beginDeleteSecret(prefix + (i + 1)).waitForCompletion().getValue();
            context.setLastDeleted(actual);
            return actual;
        }).map(DeletedSecret::getRecoveryId).collect(Collectors.toList());
        context.setDeletedRecoveryIds(deleted);
    }

    @When("the deleted secret properties are listed")
    public void theDeletedSecretPropertiesAreListed() {
        final PagedIterable<DeletedSecret> actual = context.getClient().listDeletedSecrets();
        final List<String> list = actual.stream()
                .map(DeletedSecret::getRecoveryId)
                .collect(Collectors.toList());
        context.setListedIds(list);
    }

    @When("secret is recovered")
    public void secretIsRecovered() {
        final DeletedSecret deleted = context.getLastDeleted();
        final KeyVaultSecret secret = context.getClient().beginRecoverDeletedSecret(deleted.getName()).waitForCompletion().getValue();
        context.addFetchedSecret(secret.getName(), secret);
    }

    @When("the last version of the secret is prepared for an update")
    public void theLastVersionOfTheSecretIsPreparedForAnUpdate() {
        final KeyVaultSecret lastResult = context.getLastResult();
        final SecretProperties updatedProperties = context.getClient()
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
        final SecretProperties properties = context.getClient().updateSecretProperties(context.getUpdateProperties());
        fetchLatestSecretVersion(properties.getName());
    }
}
