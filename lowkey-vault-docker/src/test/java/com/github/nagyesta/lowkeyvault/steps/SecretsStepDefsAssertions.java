package com.github.nagyesta.lowkeyvault.steps;

import com.azure.core.exception.ResourceNotFoundException;
import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.jspecify.annotations.Nullable;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.context.KeyTestContext.NOW;

public class SecretsStepDefsAssertions extends CommonAssertions {

    private final SecretTestContext context;

    public SecretsStepDefsAssertions(final TestContextConfig config) {
        context = config.secretContext();
    }

    @Then("the created secret exists with value: {secretValue}")
    public void theCreatedSecretExists(final String secretValue) {
        assertEquals(secretValue, context.getLastResult().getValue());
    }

    @Then("the secret name is {name}")
    public void theSecretNameIsSecretName(final String secretName) {
        assertEquals(secretName, context.getLastResult().getName());
    }

    @Then("the secret URL contains the vault url and {name}")
    public void theSecretURLContainsVaultUrlAndSecretName(final String secretName) {
        assertTrue(context.getLastResult().getId() + " did not start with " + context.getProvider().vaultUrl(),
                context.getLastResult().getId().startsWith(context.getProvider().vaultUrl()));
        assertTrue(context.getLastResult().getId() + " did not contain " + secretName,
                context.getLastResult().getId().contains(secretName));
    }

    @Then("the secret enabled status is {enabled}")
    public void theSecretEnabledStatusIsEnabledStatus(final boolean enabledStatus) {
        assertEquals(enabledStatus, context.getLastResult().getProperties().isEnabled());
    }

    @Then("the secret expires {optionalInt} seconds after creation")
    public void theSecretExpiresExpiresSecondsAfterCreation(@Nullable final Integer expires) {
        if (expires == null) {
            assertNull(context.getLastResult().getProperties().getExpiresOn());
        } else {
            assertEquals(
                    NOW.plusSeconds(expires).truncatedTo(ChronoUnit.SECONDS),
                    context.getLastResult().getProperties().getExpiresOn());
        }
    }

    @Then("the secret is not usable before {optionalInt} seconds after creation")
    public void theSecretIsNotUsableBeforeNotBeforeSecondsAfterCreation(@Nullable final Integer notBefore) {
        if (notBefore == null) {
            assertNull(context.getLastResult().getProperties().getNotBefore());
        } else {
            assertEquals(
                    NOW.plusSeconds(notBefore).truncatedTo(ChronoUnit.SECONDS),
                    context.getLastResult().getProperties().getNotBefore());
        }
    }

    @Then("the secret has {tagMap} as tags")
    public void theSecretHasTagMapAsTags(final Map<String, String> expectedMap) {
        final var tags = context.getLastResult().getProperties().getTags();
        assertContainsEqualEntries(expectedMap, tags);
    }

    @Then("the secret has {contentType} as content type")
    public void theRsaSpecificFieldsAreNotPopulated(final String contentType) {
        assertEquals(contentType, context.getLastResult().getProperties().getContentType());
    }

    @And("the secret recovery settings are default")
    public void theRecoverySettingsAreDefault() {
        assertEquals("Recoverable", context.getLastResult().getProperties().getRecoveryLevel());
        assertEquals(90, context.getLastResult().getProperties().getRecoverableDays());
    }

    @Then("the deleted secret recovery id contains the vault url and {name}")
    public void theDeletedSecretRecoveryIdContainsTheVaultUrlAndSecretName(final String secretName) {
        final var recoveryId = context.getLastDeleted().getRecoveryId();
        assertTrue(recoveryId + " did not start with " + context.getProvider().vaultUrl(),
                recoveryId.startsWith(context.getProvider().vaultUrl()));
        assertTrue(recoveryId + " did not contain " + secretName,
                recoveryId.contains(secretName));
    }

    @Then("the secret recovery timestamps are default")
    public void theRecoveryTimestampsAreDefault() {
        assertTrue(NOW.isBefore(context.getLastDeleted().getDeletedOn()));
        assertTrue(NOW.plusDays(90).isBefore(context.getLastDeleted().getScheduledPurgeDate()));
    }

    @Then("the list of secrets should contain {int} managed items")
    public void theListShouldContainCountManagedItems(final int count) {
        final var ids = context.getListedManagedIds();
        assertEquals(count, ids.size());
    }

    @Then("the listed secrets are matching the ones created")
    public void theListedSecretsAreMatchingTheOnesCreated() {
        final var actual = context.getListedIds();
        final var expected = context.getCreatedEntities().values().stream()
                .map(secrets -> new LinkedList<>(secrets).getLast().getId())
                .map(s -> s.replaceFirst("/[0-9a-f]{32}$", ""))
                .toList();
        assertContainsEqualEntriesSorted(expected, actual);
    }

    @Then("the listed deleted secrets are matching the ones deleted before")
    public void theListedDeletedSecretsAreMatchingTheOnesDeletedBefore() {
        final var actual = context.getListedIds();
        final var expected = context.getDeletedRecoveryIds();
        assertContainsEqualEntriesSorted(expected, actual);
    }

    @And("the listed deleted secrets are empty")
    public void theListedDeletedSecretsAreEmpty() {
        assertEquals(Collections.emptyList(), context.getListedIds());
    }

    @Then("the last secret version of {name} cannot be fetched as it is not enabled")
    public void theLastSecretVersionOfSecretNameCannotBeFetchedAsItIsNotEnabled(final String secretName) {
        try {
            context.getClient(context.getSecretServiceVersion()).getSecret(secretName);
            assertFail("Should not be possible to get disabled secret: " + secretName);
        } catch (final ResourceNotFoundException expected) {
            assertNotNull(expected);
        }
    }
}
