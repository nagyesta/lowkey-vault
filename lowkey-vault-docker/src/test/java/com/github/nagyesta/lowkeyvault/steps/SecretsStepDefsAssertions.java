package com.github.nagyesta.lowkeyvault.steps;

import com.github.nagyesta.lowkeyvault.context.SecretTestContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.context.KeyTestContext.NOW;

public class SecretsStepDefsAssertions extends CommonAssertions {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private SecretTestContext context;

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
        assertTrue(context.getLastResult().getId() + " did not start with " + context.getProvider().getVaultUrl(),
                context.getLastResult().getId().startsWith(context.getProvider().getVaultUrl()));
        assertTrue(context.getLastResult().getId() + " did not contain " + secretName,
                context.getLastResult().getId().contains(secretName));
    }

    @Then("the secret enabled status is {enabled}")
    public void theSecretEnabledStatusIsEnabledStatus(final boolean enabledStatus) {
        assertEquals(enabledStatus, context.getLastResult().getProperties().isEnabled());
    }

    @Then("the secret expires {optionalInt} seconds after creation")
    public void theSecretExpiresExpiresSecondsAfterCreation(final Integer expires) {
        if (expires == null) {
            assertNull(context.getLastResult().getProperties().getExpiresOn());
        } else {
            assertEquals(
                    NOW.plusSeconds(expires).truncatedTo(ChronoUnit.SECONDS),
                    context.getLastResult().getProperties().getExpiresOn());
        }
    }

    @Then("the secret is not usable before {optionalInt} seconds after creation")
    public void theSecretIsNotUsableBeforeNotBeforeSecondsAfterCreation(final Integer notBefore) {
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
        final Map<String, String> tags = context.getLastResult().getProperties().getTags();
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
        final String recoveryId = context.getLastDeleted().getRecoveryId();
        assertTrue(recoveryId + " did not start with " + context.getProvider().getVaultUrl(),
                recoveryId.startsWith(context.getProvider().getVaultUrl()));
        assertTrue(recoveryId + " did not contain " + secretName,
                recoveryId.contains(secretName));
    }

    @Then("the secret recovery timestamps are default")
    public void theRecoveryTimestampsAreDefault() {
        assertTrue(NOW.isBefore(context.getLastDeleted().getDeletedOn()));
        assertTrue(NOW.plusDays(90).isBefore(context.getLastDeleted().getScheduledPurgeDate()));
    }

    @Then("the listed secrets are matching the ones created")
    public void theListedSecretsAreMatchingTheOnesCreated() {
        final List<String> actual = context.getListedIds();
        final List<String> expected = context.getCreatedEntities().values().stream()
                .map(secrets -> new LinkedList<>(secrets).getLast().getId())
                .map(s -> s.replaceFirst("/[0-9a-f]{32}$", ""))
                .collect(Collectors.toList());
        assertContainsEqualEntriesSorted(expected, actual);
    }

    @Then("the listed deleted secrets are matching the ones deleted before")
    public void theListedDeletedSecretsAreMatchingTheOnesDeletedBefore() {
        final List<String> actual = context.getListedIds();
        final List<String> expected = context.getDeletedRecoveryIds();
        assertContainsEqualEntriesSorted(expected, actual);
    }

    @And("the listed deleted secrets are empty")
    public void theListedDeletedSecretsAreEmpty() {
        assertEquals(Collections.emptyList(), context.getListedIds());
    }
}
