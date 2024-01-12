package com.github.nagyesta.lowkeyvault.steps;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.keys.models.*;
import com.github.nagyesta.lowkeyvault.context.KeyTestContext;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.nagyesta.lowkeyvault.context.KeyTestContext.NOW;

public class KeysStepDefsAssertions extends CommonAssertions {

    private final KeyTestContext context;

    public KeysStepDefsAssertions(final TestContextConfig config) {
        this.context = config.keyContext();
    }

    @Then("the created key is using EC algorithm with {ecCurveName} curve name and {int} bytes length")
    public void theCreatedKeyIsUsingEcAlgorithmWithNBytesBytesLength(final KeyCurveName curveName, final int nBytes) {
        assertEquals(curveName, context.getLastResult().getKey().getCurveName());
        //X and Y are not returned by the server to avoid miscalculation issues with wrong public key
    }

    @Then("the created key is using OCT algorithm")
    public void theCreatedKeyIsUsingOctAlgorithm() {
        //Crypto material is not returned for symmetric key
        assertNull(context.getLastResult().getKey().getK());
    }

    @Then("the created key is using RSA algorithm with {int} bytes length")
    public void theCreatedKeyIsUsingRsaAlgorithmWithNBytesBytesLength(final int nBytes) {
        assertByteArrayLength(nBytes, context.getLastResult().getKey().getN());
    }

    @Then("the key name is {name}")
    public void theKeyNameIsKeyName(final String keyName) {
        assertEquals(keyName, context.getLastResult().getName());
    }

    @Then("the key URL contains the vault url and {name}")
    public void theKeyURLContainsVaultUrlAndKeyName(final String keyName) {
        assertTrue(context.getLastResult().getId() + " did not start with " + context.getProvider().getVaultUrl(),
                context.getLastResult().getId().startsWith(context.getProvider().getVaultUrl()));
        assertTrue(context.getLastResult().getId() + " did not contain " + keyName,
                context.getLastResult().getId().contains(keyName));
    }

    @Then("the key enabled status is {enabled}")
    public void theKeyEnabledStatusIsEnabledStatus(final boolean enabledStatus) {
        assertEquals(enabledStatus, context.getLastResult().getProperties().isEnabled());
    }

    @Then("the key expires {optionalInt} seconds after creation")
    public void theKeyExpiresExpiresSecondsAfterCreation(final Integer expires) {
        if (expires == null) {
            assertNull(context.getLastResult().getProperties().getExpiresOn());
        } else {
            assertEquals(
                    NOW.plusSeconds(expires).truncatedTo(ChronoUnit.SECONDS),
                    context.getLastResult().getProperties().getExpiresOn());
        }
    }

    @Then("the key is not usable before {optionalInt} seconds after creation")
    public void theKeyIsNotUsableBeforeNotBeforeSecondsAfterCreation(final Integer notBefore) {
        if (notBefore == null) {
            assertNull(context.getLastResult().getProperties().getNotBefore());
        } else {
            assertEquals(
                    NOW.plusSeconds(notBefore).truncatedTo(ChronoUnit.SECONDS),
                    context.getLastResult().getProperties().getNotBefore());
        }
    }

    @Then("the key has {keyOperations} as operations")
    public void theKeyHasOperationsAsOperations(final List<KeyOperation> expectedList) {
        final List<KeyOperation> keyOperations = context.getLastResult().getKeyOperations();
        assertContainsEqualEntries(expectedList, keyOperations);
    }

    @Then("the key has {tagMap} as tags")
    public void theKeyHasTagMapAsTags(final Map<String, String> expectedMap) {
        final Map<String, String> tags = context.getLastResult().getProperties().getTags();
        assertContainsEqualEntries(expectedMap, tags);
    }

    @Then("the key was created {hsm} HSM")
    public void theKeyWasCreatedHsmHSM(final boolean hsm) {
        assertEquals(hsm, context.getLastResult().getKeyType().toString().endsWith("HSM"));
    }

    @Then("the EC specific fields are not populated")
    public void theEcSpecificFieldsAreNotPopulated() {
        assertNull(context.getLastResult().getKey().getX());
        assertNull(context.getLastResult().getKey().getY());
        assertNull(context.getLastResult().getKey().getCurveName());
    }

    @Then("the OCT specific fields are not populated")
    public void theOctSpecificFieldsAreNotPopulated() {
        assertNull(context.getLastResult().getKey().getK());
    }

    @Then("the RSA specific fields are not populated")
    public void theRsaSpecificFieldsAreNotPopulated() {
        assertNull(context.getLastResult().getKey().getN());
        assertNull(context.getLastResult().getKey().getE());
    }

    @Then("the key recovery settings are default")
    public void theRecoverySettingsAreDefault() {
        assertEquals("Recoverable", context.getLastResult().getProperties().getRecoveryLevel());
        assertEquals(90, context.getLastResult().getProperties().getRecoverableDays());
    }

    @Then("the deleted key recovery id contains the vault url and {name}")
    public void theDeletedKeyRecoveryIdContainsTheVaultUrlAndKeyName(final String keyName) {
        final String recoveryId = context.getLastDeleted().getRecoveryId();
        assertTrue(recoveryId + " did not start with " + context.getProvider().getVaultUrl(),
                recoveryId.startsWith(context.getProvider().getVaultUrl()));
        assertTrue(recoveryId + " did not contain " + keyName,
                recoveryId.contains(keyName));
    }

    @Then("the key recovery timestamps are default")
    public void theRecoveryTimestampsAreDefault() {
        assertTrue(NOW.isBefore(context.getLastDeleted().getDeletedOn()));
        assertTrue(NOW.plusDays(90).isBefore(context.getLastDeleted().getScheduledPurgeDate()));
    }

    @Then("the listed keys are matching the ones created")
    public void theListedKeysAreMatchingTheOnesCreated() {
        final List<String> actual = context.getListedIds();
        final List<String> expected = context.getCreatedEntities().values().stream()
                .map(keyVaultKeys -> new LinkedList<>(keyVaultKeys).getLast().getId())
                .map(s -> s.replaceFirst("/[0-9a-f]{32}$", ""))
                .collect(Collectors.toList());
        assertContainsEqualEntriesSorted(expected, actual);
    }

    @Then("the list of keys should contain {int} managed items")
    public void theListShouldContainCountManagedItems(final int count) {
        final List<String> ids = context.getListedManagedIds();
        assertEquals(count, ids.size());
    }

    @Then("the listed deleted keys are matching the ones deleted before")
    public void theListedDeletedKeysAreMatchingTheOnesDeletedBefore() {
        final List<String> actual = context.getListedIds();
        final List<String> expected = context.getDeletedRecoveryIds();
        assertContainsEqualEntriesSorted(expected, actual);
    }

    @Then("the decrypted value is {clearText}")
    public void theDecryptedValueIsClearText(final byte[] text) {
        assertEquals(text, context.getDecryptResult().getPlainText());
    }

    @Then("the signature matches")
    public void theSignatureMatches() {
        assertTrue(context.getVerifyResult());
    }

    @And("the listed deleted keys are empty")
    public void theListedDeletedKeysAreEmpty() {
        assertEquals(Collections.emptyList(), context.getListedIds());
    }

    @And("the key named {name} matches the previous backup")
    public void theKeyNamedNameMatchesThePreviousBackup(final String name) {
        final byte[] bytes = context.getClient(context.getKeyServiceVersion()).backupKey(name);
        assertArrayEquals(context.getBackupBytes(name), bytes);
    }

    @And("the unpacked backup of {name} matches the content of the classpath resource")
    public void theKeyNamedNameMatchesTheResourceContent(final String name) throws IOException {
        final byte[] bytes = context.getClient(context.getKeyServiceVersion()).backupKey(name);
        final String backup = context.getLowkeyVaultManagementClient().unpackBackup(bytes)
                .replaceAll("[ \\n]+", "");
        final String expected = readResourceContent("/json/backups/" + name + ".json");
        assertEquals(expected, backup);
    }

    @Then("the length of the random data is {int} bytes")
    public void theLengthOfTheRandomDataIsBytes(final int count) {
        final int actual = context.getBackupBytes("random").length;
        assertEquals(count, actual);
    }

    @Then("the key named auto-rotate has {int} versions")
    public void theKeyNamedAutoRotateHasVersions(final int versions) {
        final List<KeyProperties> keyVersions = context.getClient(context.getKeyServiceVersion())
                .listPropertiesOfKeyVersions(context.getLastResult().getName())
                .stream()
                .collect(Collectors.toList());
        assertEquals(versions, keyVersions.size());
    }

    @And("the rotation policy of {name} is rotating after {int} days with expiry of {int} days")
    public void theRotationPolicyOfKeyIsRotatingAfterDaysWithExpiryOfDays(final String keyName, final int rotate, final int expiry) {
        final KeyRotationPolicy keyRotationPolicy = context.getClient(context.getKeyServiceVersion()).getKeyRotationPolicy(keyName);
        assertEquals("P" + expiry + "D", keyRotationPolicy.getExpiresIn());
        assertEquals(1, keyRotationPolicy.getLifetimeActions().size());
        final KeyRotationLifetimeAction action = keyRotationPolicy.getLifetimeActions().get(0);
        assertEquals("P" + rotate + "D", action.getTimeAfterCreate());
        assertEquals(KeyRotationPolicyAction.ROTATE, action.getAction());
    }

    @Then("the last key version of {name} cannot be fetched as it is not enabled")
    public void theLastKeyVersionOfKeyNameCannotBeFetchedAsItIsNotEnabled(final String keyName) {
        try {
            context.getClient(context.getKeyServiceVersion()).getKey(keyName);
            assertFail("Should not be possible to get disabled key: " + keyName);
        } catch (final ResourceNotFoundException expected) {
            assertNotNull(expected);
        }
    }
}
