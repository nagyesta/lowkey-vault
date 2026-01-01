package com.github.nagyesta.lowkeyvault.steps;

import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.cryptography.models.*;
import com.azure.security.keyvault.keys.models.*;
import com.github.nagyesta.lowkeyvault.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.context.KeyTestContext;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jspecify.annotations.Nullable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.util.*;
import java.util.stream.IntStream;

import static com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm.*;
import static com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.*;
import static com.github.nagyesta.lowkeyvault.context.KeyTestContext.NOW;
import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.CONTAINER_AUTHORITY;
import static java.lang.Boolean.TRUE;

public class KeysStepDefs extends CommonAssertions {

    private static final BouncyCastleProvider BOUNCY_CASTLE_PROVIDER = new BouncyCastleProvider();
    private final KeyTestContext context;

    public KeysStepDefs(final TestContextConfig config) {
        context = config.keyContext();
    }

    @Given("key API version {api} is used")
    public void apiVersionApiIsUsed(final String version) {
        context.setApiVersion(version);
    }

    @Given("a key client is created with the vault named {name}")
    public void theKeyClientIsCreatedWithVaultNameSelected(final String vaultName) {
        final var vaultAuthority = vaultName + ".localhost:8443";
        final var vaultUrl = "https://" + vaultAuthority;
        final var overrideFunction = new AuthorityOverrideFunction(vaultAuthority, CONTAINER_AUTHORITY);
        context.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
    }

    @Given("an EC key named {name} is prepared with {ecCurveName} and {hsm} HSM")
    public void anEcKeyNamedKeyNameIsPreparedWithCurveNameAndHsmSet(
            final String keyName,
            final KeyCurveName curveName,
            final boolean hsm) {
        context.setCreateEcKeyOptions(new CreateEcKeyOptions(keyName)
                .setCurveName(curveName)
                .setHardwareProtected(hsm));
    }

    @Given("an OCT key named {name} is prepared with {octKeySize} bits size")
    public void anOctKeyIsCreatedWithNameKeySizeAndHsmSet(
            final String keyName,
            final int keySize) {
        context.setCreateOctKeyOptions(new CreateOctKeyOptions(keyName).setKeySize(keySize).setHardwareProtected(true));
    }

    @Given("an RSA key named {name} is prepared with {rsaKeySize} bits size {hsm} HSM")
    public void anRsaKeyIsCreatedWithNameKeySizeAndHsmSet(
            final String keyName,
            final int keySize,
            final boolean hsm) {
        context.setCreateRsaKeyOptions(new CreateRsaKeyOptions(keyName).setKeySize(keySize).setHardwareProtected(hsm));
    }

    @SuppressWarnings("DataFlowIssue")
    @Given("{int} version of the EC key is created")
    public void ecKeyCreationRequestIsSentVersionsCountTimes(final int versionsCount) {
        final var createKeyOptions = context.getCreateEcKeyOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final var ecKey = context.getClient(context.getKeyServiceVersion()).createEcKey(createKeyOptions);
            context.addCreatedEntity(createKeyOptions.getName(), ecKey);
        });
    }

    @SuppressWarnings("DataFlowIssue")
    @Given("{int} version of the OCT key is created")
    public void octKeyCreationRequestIsSentVersionsCountTimes(final int versionsCount) {
        final var createKeyOptions = context.getCreateOctKeyOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final var octKey = context.getClient(context.getKeyServiceVersion()).createOctKey(createKeyOptions);
            context.addCreatedEntity(createKeyOptions.getName(), octKey);
        });
    }

    @SuppressWarnings("DataFlowIssue")
    @Given("{int} version of the RSA key is created")
    public void rsaKeyCreationRequestIsSentVersionsCountTimes(final int versionsCount) {
        final var createKeyOptions = context.getCreateRsaKeyOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final var rsaKey = context.getClient(context.getKeyServiceVersion()).createRsaKey(createKeyOptions);
            context.addCreatedEntity(createKeyOptions.getName(), rsaKey);
        });
    }

    @SuppressWarnings("DataFlowIssue")
    @And("the EC key is created")
    public void ecKeyCreationRequestIsSent() {
        final var createKeyOptions = context.getCreateEcKeyOptions();
        final var ecKey = context.getClient(context.getKeyServiceVersion()).createEcKey(createKeyOptions);
        context.addCreatedEntity(createKeyOptions.getName(), ecKey);
    }

    @SuppressWarnings("DataFlowIssue")
    @And("the OCT key is created")
    public void octKeyCreationRequestIsSent() {
        final var createKeyOptions = context.getCreateOctKeyOptions();
        final var octKey = context.getClient(context.getKeyServiceVersion()).createOctKey(createKeyOptions);
        context.addCreatedEntity(createKeyOptions.getName(), octKey);
    }

    @SuppressWarnings("DataFlowIssue")
    @And("the RSA key is created")
    public void rsaKeyCreationRequestIsSent() {
        final var createKeyOptions = context.getCreateRsaKeyOptions();
        final var rsaKey = context.getClient(context.getKeyServiceVersion()).createRsaKey(createKeyOptions);
        context.addCreatedEntity(createKeyOptions.getName(), rsaKey);
    }

    @And("an EC key is imported with {name} as name and {ecCurveName} curve {hsm} HSM")
    public void ecKeyImportedWithNameAndParameters(
            final String name,
            final KeyCurveName curveName,
            final boolean hsm) {
        final var keyPair = KeyGenUtil.generateEc(curveName);
        context.setKeyPair(keyPair);
        final var key = JsonWebKey.fromEc(keyPair, BOUNCY_CASTLE_PROVIDER)
                .setKeyOps(List.of(KeyOperation.SIGN));
        if (hsm) {
            key.setKeyType(KeyType.EC_HSM);
        }
        final var options = new ImportKeyOptions(name, key)
                .setHardwareProtected(hsm);
        final var ecKey = context.getClient(context.getKeyServiceVersion()).importKey(options);
        context.addCreatedEntity(name, ecKey);
    }

    @And("an RSA key is imported with {name} as name and {rsaKeySize} bits of key size {hsm} HSM")
    public void rsaKeyImportedWithNameAndParameters(
            final String name,
            final int size,
            final boolean hsm) {
        final var keyPair = KeyGenUtil.generateRsa(size, null);
        context.setKeyPair(keyPair);
        final var key = JsonWebKey.fromRsa(keyPair)
                .setKeyOps(List.of(KeyOperation.SIGN, KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        if (hsm) {
            key.setKeyType(KeyType.RSA_HSM);
        }
        final var options = new ImportKeyOptions(name, key)
                .setHardwareProtected(hsm);
        final var rsaKey = context.getClient(context.getKeyServiceVersion()).importKey(options);
        context.addCreatedEntity(name, rsaKey);
    }

    @And("an OCT key is imported with {name} as name and {octKeySize} bits of key size with HSM")
    public void octKeyImportedWithNameAndParameters(
            final String name,
            final int size) {
        final var secretKey = KeyGenUtil.generateAes(size);
        context.setSecretKey(secretKey);
        final var key = JsonWebKey.fromAes(secretKey)
                .setKeyOps(List.of(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY))
                .setKeyType(KeyType.OCT_HSM);
        final var options = new ImportKeyOptions(name, key)
                .setHardwareProtected(true);
        final var rsaKey = context.getClient(context.getKeyServiceVersion()).importKey(options);
        context.addCreatedEntity(name, rsaKey);
    }

    @When("the first key version of {name} is fetched with providing a version")
    public void fetchFirstKeyVersion(final String name) {
        final var versionsCreated = context.getCreatedEntities().get(name);
        final var version = versionsCreated.getFirst().getProperties().getVersion();
        final var vaultKey = context.getClient(context.getKeyServiceVersion()).getKey(name, version);
        context.addFetchedKey(name, vaultKey);
        assertEquals(version, vaultKey.getProperties().getVersion());
    }

    @When("the last key version of {name} is fetched without providing a version")
    public void fetchLatestKeyVersion(final String name) {
        final var vaultKey = context.getClient(context.getKeyServiceVersion()).getKey(name);
        final var versionsCreated = context.getCreatedEntities().get(name);
        final var expectedLastVersionId = versionsCreated.getLast().getId();
        context.addFetchedKey(name, vaultKey);
        assertEquals(expectedLastVersionId, vaultKey.getId());
    }

    @Given("the key is set to expire {optionalInt} seconds after creation")
    public void theKeyIsSetToExpireExpiresSecondsAfterCreation(@Nullable final Integer expire) {
        Optional.ofNullable(expire)
                .ifPresent(e -> Objects.requireNonNull(context.getCreateKeyOptions()).setExpiresOn(NOW.plusSeconds(e)));
    }

    @Given("the key is set to be not usable until {optionalInt} seconds after creation")
    public void theKeyIsSetToBeNotUsableUntilNotBeforeSecondsAfterCreation(@Nullable final Integer notBefore) {
        Optional.ofNullable(notBefore)
                .ifPresent(n -> Objects.requireNonNull(context.getCreateKeyOptions()).setNotBefore(NOW.plusSeconds(n)));
    }

    @Given("the key is set to use {tagMap} as tags")
    public void theKeyIsSetToUseTagMapAsTags(final Map<String, String> tags) {
        Objects.requireNonNull(context.getCreateKeyOptions()).setTags(tags);
    }

    @Given("the key has {keyOperations} operations granted")
    public void theKeyHasOperationsOperationsGranted(final List<KeyOperation> keyOperations) {
        Objects.requireNonNull(context.getCreateKeyOptions()).setKeyOperations(keyOperations.toArray(new KeyOperation[0]));
    }

    @Given("the key is set to be {enabled}")
    public void theKeyIsSetToBeEnabledStatus(final boolean enabledStatus) {
        Objects.requireNonNull(context.getCreateKeyOptions()).setEnabled(enabledStatus);
    }

    @And("the key is deleted")
    public void theKeyIsDeleted() {
        final var actual = context.getClient(context.getKeyServiceVersion())
                .beginDeleteKey(context.getLastResult().getName()).waitForCompletion().getValue();
        context.setLastDeleted(actual);
    }

    @When("the key properties are listed")
    public void theKeyPropertiesAreListed() {
        final var actual = context.getClient(context.getKeyServiceVersion()).listPropertiesOfKeys();
        final var propertyList = actual.stream()
                .toList();
        final var list = propertyList.stream()
                .map(KeyProperties::getId)
                .toList();
        context.setListedIds(list);
        final var managedList = propertyList.stream()
                .filter(keyProperties -> TRUE == keyProperties.isManaged())
                .map(KeyProperties::getId)
                .toList();
        context.setListedManagedIds(managedList);
    }

    @Given("{int} EC keys with {name} prefix are created with {ecCurveName} and {hsm} HSM")
    public void ecKeysWithKeyNamePrefixAreCreatedWithBitsSizeAndHSM(
            final int count,
            final String prefix,
            final KeyCurveName curveName,
            final boolean hsm) {
        IntStream.range(0, count).forEach(i -> {
            anEcKeyNamedKeyNameIsPreparedWithCurveNameAndHsmSet(prefix + (i + 1), curveName, hsm);
            ecKeyCreationRequestIsSent();
        });
    }

    @Given("{int} OCT keys with {name} prefix are created with {int} bits size")
    public void octKeysWithKeyNamePrefixAreCreatedWithBitsSizeAndHSM(
            final int count,
            final String prefix,
            final int size) {
        IntStream.range(0, count).forEach(i -> {
            anOctKeyIsCreatedWithNameKeySizeAndHsmSet(prefix + (i + 1), size);
            octKeyCreationRequestIsSent();
        });
    }

    @Given("{int} RSA keys with {name} prefix are created with {int} bits size {hsm} HSM")
    public void rsaKeysWithKeyNamePrefixAreCreatedWithBitsSizeAndHSM(
            final int count,
            final String prefix,
            final int size,
            final boolean hsm) {
        IntStream.range(0, count).forEach(i -> {
            anRsaKeyIsCreatedWithNameKeySizeAndHsmSet(prefix + (i + 1), size, hsm);
            rsaKeyCreationRequestIsSent();
        });
    }

    @Given("{int} keys with {name} prefix are deleted")
    public void countKeysWithKeyNamePrefixAreDeleted(
            final int count,
            final String prefix) {
        final var deleted = IntStream.range(0, count).mapToObj(i -> {
            final var actual = context.getClient(context.getKeyServiceVersion())
                    .beginDeleteKey(prefix + (i + 1)).waitForCompletion().getValue();
            context.setLastDeleted(actual);
            return actual;
        }).map(DeletedKey::getRecoveryId).toList();
        context.setDeletedRecoveryIds(deleted);
    }

    @When("the deleted key properties are listed")
    public void theDeletedKeyPropertiesAreListed() {
        final var actual = context.getClient(context.getKeyServiceVersion()).listDeletedKeys();
        final var list = actual.stream()
                .map(DeletedKey::getRecoveryId)
                .toList();
        context.setListedIds(list);
    }

    @When("the key is recovered")
    public void theKeyIsRecovered() {
        final var deleted = context.getLastDeleted();
        final var key = context.getClient(context.getKeyServiceVersion())
                .beginRecoverDeletedKey(deleted.getName()).waitForCompletion().getValue();
        context.addFetchedKey(key.getName(), key);
    }

    @When("the key is purged")
    public void theKeyIsPurged() {
        final var deleted = context.getLastDeleted();
        context.getClient(context.getKeyServiceVersion()).purgeDeletedKey(deleted.getName());
    }

    @When("the last version of the key is prepared for an update")
    public void theLastVersionOfTheKeyIsPreparedForAnUpdate() {
        final var lastResult = context.getLastResult();
        final var updatedProperties = context.getClient(context.getKeyServiceVersion())
                .getKey(lastResult.getName(), lastResult.getProperties().getVersion()).getProperties();
        context.setUpdateProperties(updatedProperties);
    }

    @When("the key is updated to expire {optionalInt} seconds after creation")
    public void theKeyIsUpdatedToExpireExpiresSecondsAfterCreation(@Nullable final Integer expire) {
        Optional.ofNullable(expire).ifPresent(e -> context.getUpdateProperties().setExpiresOn(NOW.plusSeconds(e)));
    }

    @When("the key is updated to be not usable until {optionalInt} seconds after creation")
    public void theKeyIsUpdatedToBeNotUsableUntilNotBeforeSecondsAfterCreation(@Nullable final Integer notBefore) {
        Optional.ofNullable(notBefore).ifPresent(n -> context.getUpdateProperties().setNotBefore(NOW.plusSeconds(n)));
    }

    @When("the key is updated to have {keyOperations} operations granted")
    public void theKeyIsUpdatedToHaveOperationsOperationsGranted(final List<KeyOperation> keyOperations) {
        context.setUpdateKeyOperations(keyOperations.toArray(new KeyOperation[0]));
    }

    @When("the key is updated to use {tagMap} as tags")
    public void theKeyIsUpdatedToUseTagMapAsTags(final Map<String, String> tags) {
        context.getUpdateProperties().setTags(tags);
    }

    @When("the key is updated to be {enabled}")
    public void theKeyIsUpdatedToBeEnabledStatus(final boolean enabledStatus) {
        context.getUpdateProperties().setEnabled(enabledStatus);
    }

    @When("the key update request is sent")
    public void theUpdateRequestIsSent() {
        final var key = context.getClient(context.getKeyServiceVersion())
                .updateKeyProperties(context.getUpdateProperties(), context.getUpdateKeyOperations());
        final var properties = key.getProperties();
        context.addFetchedKey(properties.getName(), key);
    }

    @When("the created key is used to encrypt {clearText} with {algorithm}")
    public void theCreatedKeyIsUsedToEncryptClearTextWithAlgorithm(
            final byte[] text,
            final EncryptionAlgorithm algorithm) {
        final var keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId, context.getCryptoServiceVersion()));
        final var encryptResult = context.getCryptographyClient()
                .encrypt(encryptParams(algorithm, text), Context.NONE);
        context.setEncryptResult(encryptResult);
    }

    @When("the created key is used to sign {clearText} with {signAlgorithm}")
    public void theCreatedKeyIsUsedToSignClearTextWithAlgorithm(
            final byte[] text,
            final SignatureAlgorithm algorithm) {
        final var keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId, context.getCryptoServiceVersion()));
        final var signResult = context.getCryptographyClient().signData(algorithm, text);
        context.setSignatureResult(signResult.getSignature());
    }

    @When("the encrypted value is not {clearText}")
    public void theEncryptedValueIsNotClearText(final byte[] text) {
        assertTrue("The cipherText and the clearText should not be the same!",
                !Arrays.equals(context.getEncryptResult().getCipherText(), text));
    }

    @When("the signed value is not {clearText}")
    public void theSignedValueIsNotClearText(final byte[] text) {
        assertTrue("The signature and the clearText should not be the same!",
                !Arrays.equals(context.getSignatureResult(), text));
    }

    @And("the encrypted value is decrypted with {algorithm}")
    public void theEncryptedValueIsDecryptedWithAlgorithm(final EncryptionAlgorithm algorithm) {
        final var keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId, context.getCryptoServiceVersion()));
        final var decryptResult = context.getCryptographyClient()
                .decrypt(decryptParams(algorithm, context.getEncryptResult()), Context.NONE);
        context.setDecryptResult(decryptResult);
    }

    @And("the encrypted value is decrypted using the original OCT key using {algorithm}")
    public void theEncryptedValueIsDecryptedUsingTheOriginalOctKeyWithAlgorithm(final EncryptionAlgorithm algorithm) throws Exception {
        final var cipher = Cipher.getInstance(getSymmetricalEncryptionAlgName(algorithm), BOUNCY_CASTLE_PROVIDER);
        final var encryptResult = context.getEncryptResult();
        cipher.init(Cipher.DECRYPT_MODE, context.getSecretKey(), new IvParameterSpec(encryptResult.getIv()));
        final var plain = cipher.doFinal(encryptResult.getCipherText());
        context.setDecryptResult(new DecryptResult(plain, algorithm, context.getLastResult().getId()));
    }

    @And("the signature of {clearText} is verified with {signAlgorithm}")
    public void theSignValueIsVerifiedWithAlgorithm(
            final byte[] text,
            final SignatureAlgorithm algorithm) {
        final var keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId, context.getCryptoServiceVersion()));
        final var verifyResult = context.getCryptographyClient().verifyData(algorithm, text, context.getSignatureResult());
        context.setVerifyResult(verifyResult.isValid());
    }

    @And("the EC signature of {clearText} is verified using the original public key with {signAlgorithm}")
    public void theEcSignValueIsVerifiedUsingOriginalPublicKeyWithAlgorithm(
            final byte[] text,
            final SignatureAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        final var ecVerify = Signature.getInstance(getAsymmetricSignatureAlgorithm(algorithm), BOUNCY_CASTLE_PROVIDER);
        ecVerify.initVerify(context.getKeyPair().getPublic());
        ecVerify.update(text);
        final var result = ecVerify.verify(context.getSignatureResult());
        context.setVerifyResult(result);
    }

    @And("the RSA signature of {clearText} is verified using the original public key with {signAlgorithm}")
    public void theRsaSignValueIsVerifiedUsingOriginalPublicKeyWithAlgorithm(
            final byte[] text,
            final SignatureAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        final var rsaVerify = Signature.getInstance(getAsymmetricSignatureAlgorithm(algorithm), BOUNCY_CASTLE_PROVIDER);
        rsaVerify.initVerify(context.getKeyPair().getPublic());
        rsaVerify.update(text);
        final var result = rsaVerify.verify(context.getSignatureResult());
        context.setVerifyResult(result);
    }

    @And("the key named {name} is backed up")
    public void theKeyNamedNameIsBackedUp(final String name) {
        final var bytes = context.getClient(context.getKeyServiceVersion()).backupKey(name);
        context.setBackupBytes(name, bytes);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "resource"})
    @And("the key named {name} is backed up to resource")
    public void theKeyNamedNameIsBackedUpToResource(final String name) throws IOException {
        final var bytes = context.getClient(context.getKeyServiceVersion()).backupKey(name);
        final var s = context.getLowkeyVaultManagementClient().unpackBackup(bytes);
        final var file = new File("/home/esta/IdeaProjects/github/lowkey-vault/lowkey-vault-docker/src/test/resources"
                + "/json/backups/" + name + ".json");
        file.createNewFile();
        new FileWriter(file).append(s).close();
        context.setBackupBytes(name, bytes);
    }

    @And("the key named {name} is restored")
    public void theKeyNamedNameIsRestored(final String name) {
        final var bytes = context.getBackupBytes(name);
        final var key = context.getClient(context.getKeyServiceVersion()).restoreKeyBackup(bytes);
        context.addFetchedKey(name, key);
    }

    @And("the key named {name} is restored from classpath resource")
    public void theKeyIsRestoredFromClasspath(final String name) throws IOException {
        final var content = readResourceContent("/json/backups/" + name + ".json");
        final var bytes = context.getLowkeyVaultManagementClient().compressBackup(content);
        final var key = context.getClient(context.getKeyServiceVersion()).restoreKeyBackup(bytes);
        context.addFetchedKey(key.getName(), key);
    }

    @When("the vault is called for {int} bytes of random data")
    public void theVaultIsCalledForBytesOfRandomData(final int count) {
        final var bytes = context.getClient(context.getKeyServiceVersion()).getRandomBytes(count);
        context.setBackupBytes("random", bytes);
    }

    @When("the key named {name} is rotated")
    public void theKeyIsRotated(final String name) {
        final var oldId = context.getLastResult().getId();
        final var keyVaultKey = context.getClient(context.getKeyServiceVersion()).rotateKey(name);
        assertTrue(!oldId.equals(keyVaultKey.getId()));
        context.addFetchedKey(name, keyVaultKey);
    }

    private byte[] getIv() {
        return "iv-parameter-val".getBytes(StandardCharsets.UTF_8);
    }

    private @Nullable EncryptParameters encryptParams(
            final EncryptionAlgorithm encryptionAlgorithm,
            final byte[] clearText) {
        if (encryptionAlgorithm == EncryptionAlgorithm.RSA1_5) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createRsa15Parameters(clearText);
        } else if (encryptionAlgorithm == EncryptionAlgorithm.RSA_OAEP) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createRsaOaepParameters(clearText);
        } else if (encryptionAlgorithm == EncryptionAlgorithm.RSA_OAEP_256) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createRsaOaep256Parameters(clearText);
        } else if (encryptionAlgorithm == A128CBC) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createA128CbcParameters(clearText, getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A128CBCPAD) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createA128CbcPadParameters(clearText, getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A192CBC) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createA192CbcParameters(clearText, getIv());
        } else if (encryptionAlgorithm == A192CBCPAD) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createA192CbcPadParameters(clearText, getIv());
        } else if (encryptionAlgorithm == A256CBC) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createA256CbcParameters(clearText, getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A256CBCPAD) {
            return com.azure.security.keyvault.keys.cryptography.models.EncryptParameters.createA256CbcPadParameters(clearText, getIv());
        } else {
            return null;
        }
    }

    private @Nullable DecryptParameters decryptParams(
            final EncryptionAlgorithm encryptionAlgorithm,
            final EncryptResult encryptResult) {
        if (encryptionAlgorithm == EncryptionAlgorithm.RSA1_5) {
            return DecryptParameters.createRsa15Parameters(encryptResult.getCipherText());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.RSA_OAEP) {
            return DecryptParameters.createRsaOaepParameters(encryptResult.getCipherText());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.RSA_OAEP_256) {
            return DecryptParameters.createRsaOaep256Parameters(encryptResult.getCipherText());
        } else if (encryptionAlgorithm == A128CBC) {
            return DecryptParameters.createA128CbcParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A128CBCPAD) {
            return DecryptParameters.createA128CbcPadParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A192CBC) {
            return DecryptParameters.createA192CbcParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == A192CBCPAD) {
            return DecryptParameters.createA192CbcPadParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == A256CBC) {
            return DecryptParameters.createA256CbcParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else if (encryptionAlgorithm == EncryptionAlgorithm.A256CBCPAD) {
            return DecryptParameters.createA256CbcPadParameters(encryptResult.getCipherText(), encryptResult.getIv());
        } else {
            return null;
        }
    }

    private String getSymmetricalEncryptionAlgName(final EncryptionAlgorithm algorithm) {
        if (algorithm == A128CBC) {
            return "AES/CBC/ZeroBytePadding";
        } else if (algorithm == A128CBCPAD) {
            return "AES/CBC/PKCS5Padding";
        } else if (algorithm == A192CBC) {
            return "AES/CBC/ZeroBytePadding";
        } else if (algorithm == A192CBCPAD) {
            return "AES/CBC/PKCS5Padding";
        } else if (algorithm == A256CBC) {
            return "AES/CBC/ZeroBytePadding";
        } else {
            return "AES/CBC/PKCS5Padding";
        }
    }

    private String getAsymmetricSignatureAlgorithm(final SignatureAlgorithm algorithm) {
        if (algorithm == ES256) {
            return "SHA256withPLAIN-ECDSA";
        } else if (algorithm == ES256K) {
            return "SHA256withPLAIN-ECDSA";
        } else if (algorithm == ES384) {
            return "SHA384withPLAIN-ECDSA";
        } else if (algorithm == ES512) {
            return "SHA512withPLAIN-ECDSA";
        } else if (algorithm == PS256) {
            return "SHA256withRSAandMGF1";
        } else if (algorithm == PS384) {
            return "SHA384withRSAandMGF1";
        } else if (algorithm == PS512) {
            return "SHA512withRSAandMGF1";
        } else if (algorithm == RS256) {
            return "SHA256withRSA";
        } else if (algorithm == RS384) {
            return "SHA384withRSA";
        } else {
            return "SHA512withRSA";
        }
    }

    @And("the rotation policy is set to rotate after {int} days with expiry of {int} days")
    public void theRotationPolicyIsSetToRotateAfterDaysWithExpiryDays(
            final int rotateDays,
            final int expiryDays) {
        final var name = context.getLastResult().getName();
        final var action = new KeyRotationLifetimeAction(KeyRotationPolicyAction.ROTATE)
                .setTimeAfterCreate("P" + rotateDays + "D");
        context.getClient(context.getKeyServiceVersion())
                .updateKeyRotationPolicy(name, new KeyRotationPolicy()
                        .setLifetimeActions(List.of(action))
                        .setExpiresIn("P" + expiryDays + "D"));
    }
}
