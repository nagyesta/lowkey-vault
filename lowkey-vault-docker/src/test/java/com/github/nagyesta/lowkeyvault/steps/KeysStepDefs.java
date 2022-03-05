package com.github.nagyesta.lowkeyvault.steps;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.cryptography.models.*;
import com.azure.security.keyvault.keys.models.*;
import com.github.nagyesta.lowkeyvault.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.context.KeyTestContext;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm.*;
import static com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm.*;
import static com.github.nagyesta.lowkeyvault.context.KeyTestContext.NOW;
import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.CONTAINER_AUTHORITY;

public class KeysStepDefs extends CommonAssertions {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private KeyTestContext context;

    @Given("a key client is created with the vault named {name}")
    public void theKeyClientIsCreatedWithVaultNameSelected(final String vaultName) {
        final String vaultAuthority = vaultName + ".localhost:8443";
        final String vaultUrl = "https://" + vaultAuthority;
        final AuthorityOverrideFunction overrideFunction = new AuthorityOverrideFunction(vaultAuthority, CONTAINER_AUTHORITY);
        context.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
    }

    @Given("an EC key named {name} is prepared with {ecCurveName} and {hsm} HSM")
    public void anEcKeyNamedKeyNameIsPreparedWithCurveNameAndHsmSet(
            final String keyName, final KeyCurveName curveName, final boolean hsm) {
        context.setCreateEcKeyOptions(new CreateEcKeyOptions(keyName)
                .setCurveName(curveName)
                .setHardwareProtected(hsm));
    }

    @Given("an OCT key named {name} is prepared with {octKeySize} bits size")
    public void anOctKeyIsCreatedWithNameKeySizeAndHsmSet(final String keyName, final int keySize) {
        context.setCreateOctKeyOptions(new CreateOctKeyOptions(keyName).setKeySize(keySize).setHardwareProtected(true));
    }

    @Given("an RSA key named {name} is prepared with {rsaKeySize} bits size {hsm} HSM")
    public void anRsaKeyIsCreatedWithNameKeySizeAndHsmSet(final String keyName, final int keySize, final boolean hsm) {
        context.setCreateRsaKeyOptions(new CreateRsaKeyOptions(keyName).setKeySize(keySize).setHardwareProtected(hsm));
    }

    @Given("{int} version of the EC key is created")
    public void ecKeyCreationRequestIsSentVersionsCountTimes(final int versionsCount) {
        final CreateEcKeyOptions createKeyOptions = context.getCreateEcKeyOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final KeyVaultKey ecKey = context.getClient().createEcKey(createKeyOptions);
            context.addCreatedEntity(createKeyOptions.getName(), ecKey);
        });
    }

    @Given("{int} version of the OCT key is created")
    public void octKeyCreationRequestIsSentVersionsCountTimes(final int versionsCount) {
        final CreateOctKeyOptions createKeyOptions = context.getCreateOctKeyOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final KeyVaultKey octKey = context.getClient().createOctKey(createKeyOptions);
            context.addCreatedEntity(createKeyOptions.getName(), octKey);
        });
    }

    @Given("{int} version of the RSA key is created")
    public void rsaKeyCreationRequestIsSentVersionsCountTimes(final int versionsCount) {
        final CreateRsaKeyOptions createKeyOptions = context.getCreateRsaKeyOptions();
        IntStream.range(0, versionsCount).forEach(i -> {
            final KeyVaultKey rsaKey = context.getClient().createRsaKey(createKeyOptions);
            context.addCreatedEntity(createKeyOptions.getName(), rsaKey);
        });
    }

    @And("the EC key is created")
    public void ecKeyCreationRequestIsSent() {
        final CreateEcKeyOptions createKeyOptions = context.getCreateEcKeyOptions();
        final KeyVaultKey ecKey = context.getClient().createEcKey(createKeyOptions);
        context.addCreatedEntity(createKeyOptions.getName(), ecKey);
    }

    @And("the OCT key is created")
    public void octKeyCreationRequestIsSent() {
        final CreateOctKeyOptions createKeyOptions = context.getCreateOctKeyOptions();
        final KeyVaultKey octKey = context.getClient().createOctKey(createKeyOptions);
        context.addCreatedEntity(createKeyOptions.getName(), octKey);
    }

    @And("the RSA key is created")
    public void rsaKeyCreationRequestIsSent() {
        final CreateRsaKeyOptions createKeyOptions = context.getCreateRsaKeyOptions();
        final KeyVaultKey rsaKey = context.getClient().createRsaKey(createKeyOptions);
        context.addCreatedEntity(createKeyOptions.getName(), rsaKey);
    }

    @And("an EC key is imported with {name} as name and {ecCurveName} curve {hsm} HSM")
    public void ecKeyImportedWithNameAndParameters(final String name, final KeyCurveName curveName, final boolean hsm) {
        final KeyPair keyPair = KeyGenUtil.generateEc(curveName);
        context.setKeyPair(keyPair);
        final JsonWebKey key = JsonWebKey.fromEc(keyPair, new BouncyCastleProvider())
                .setKeyOps(List.of(KeyOperation.SIGN, KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        final ImportKeyOptions options = new ImportKeyOptions(name, key)
                .setHardwareProtected(hsm);
        final KeyVaultKey ecKey = context.getClient().importKey(options);
        context.addCreatedEntity(name, ecKey);
    }

    @And("an RSA key is imported with {name} as name and {rsaKeySize} bits of key size {hsm} HSM")
    public void rsaKeyImportedWithNameAndParameters(final String name, final int size, final boolean hsm) {
        final KeyPair keyPair = KeyGenUtil.generateRsa(size, null);
        context.setKeyPair(keyPair);
        final JsonWebKey key = JsonWebKey.fromRsa(keyPair)
                .setKeyOps(List.of(KeyOperation.SIGN, KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY));
        final ImportKeyOptions options = new ImportKeyOptions(name, key)
                .setHardwareProtected(hsm);
        final KeyVaultKey rsaKey = context.getClient().importKey(options);
        context.addCreatedEntity(name, rsaKey);
    }

    @And("an OCT key is imported with {name} as name and {octKeySize} bits of key size with HSM")
    public void octKeyImportedWithNameAndParameters(final String name, final int size) {
        final SecretKey secretKey = KeyGenUtil.generateAes(size);
        context.setSecretKey(secretKey);
        final JsonWebKey key = JsonWebKey.fromAes(secretKey)
                .setKeyOps(List.of(KeyOperation.SIGN, KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY))
                .setKeyType(KeyType.OCT_HSM);
        final ImportKeyOptions options = new ImportKeyOptions(name, key)
                .setHardwareProtected(true);
        final KeyVaultKey rsaKey = context.getClient().importKey(options);
        context.addCreatedEntity(name, rsaKey);
    }

    @When("the first key version of {name} is fetched with providing a version")
    public void fetchFirstKeyVersion(final String name) {
        final List<KeyVaultKey> versionsCreated = context.getCreatedEntities().get(name);
        final String version = versionsCreated.get(0).getProperties().getVersion();
        final KeyVaultKey vaultKey = context.getClient().getKey(name, version);
        context.addFetchedKey(name, vaultKey);
        assertEquals(version, vaultKey.getProperties().getVersion());
    }

    @When("the last key version of {name} is fetched without providing a version")
    public void fetchLatestKeyVersion(final String name) {
        final KeyVaultKey vaultKey = context.getClient().getKey(name);
        final List<KeyVaultKey> versionsCreated = context.getCreatedEntities().get(name);
        final String expectedLastVersionId = versionsCreated.get(versionsCreated.size() - 1).getId();
        context.addFetchedKey(name, vaultKey);
        assertEquals(expectedLastVersionId, vaultKey.getId());
    }

    @Given("the key is set to expire {optionalInt} seconds after creation")
    public void theKeyIsSetToExpireExpiresSecondsAfterCreation(final Integer expire) {
        Optional.ofNullable(expire).ifPresent(e -> context.getCreateKeyOptions().setExpiresOn(NOW.plusSeconds(e)));
    }

    @Given("the key is set to be not usable until {optionalInt} seconds after creation")
    public void theKeyIsSetToBeNotUsableUntilNotBeforeSecondsAfterCreation(final Integer notBefore) {
        Optional.ofNullable(notBefore).ifPresent(n -> context.getCreateKeyOptions().setNotBefore(NOW.plusSeconds(n)));
    }

    @Given("the key is set to use {tagMap} as tags")
    public void theKeyIsSetToUseTagMapAsTags(final Map<String, String> tags) {
        context.getCreateKeyOptions().setTags(tags);
    }

    @Given("the key has {keyOperations} operations granted")
    public void theKeyHasOperationsOperationsGranted(final List<KeyOperation> keyOperations) {
        context.getCreateKeyOptions().setKeyOperations(keyOperations.toArray(new KeyOperation[0]));
    }

    @Given("the key is set to be {enabled}")
    public void theKeyIsSetToBeEnabledStatus(final boolean enabledStatus) {
        context.getCreateKeyOptions().setEnabled(enabledStatus);
    }

    @And("the key is deleted")
    public void theKeyIsDeleted() {
        final DeletedKey actual = context.getClient().beginDeleteKey(context.getLastResult().getName()).waitForCompletion().getValue();
        context.setLastDeleted(actual);
    }

    @When("the key properties are listed")
    public void theKeyPropertiesAreListed() {
        final PagedIterable<KeyProperties> actual = context.getClient().listPropertiesOfKeys();
        final List<String> list = actual.stream()
                .map(KeyProperties::getId)
                .collect(Collectors.toList());
        context.setListedIds(list);
    }

    @Given("{int} EC keys with {name} prefix are created with {ecCurveName} and {hsm} HSM")
    public void ecKeysWithKeyNamePrefixAreCreatedWithBitsSizeAndHSM(
            final int count, final String prefix, final KeyCurveName curveName, final boolean hsm) {
        IntStream.range(0, count).forEach(i -> {
            anEcKeyNamedKeyNameIsPreparedWithCurveNameAndHsmSet(prefix + (i + 1), curveName, hsm);
            ecKeyCreationRequestIsSent();
        });
    }

    @Given("{int} OCT keys with {name} prefix are created with {int} bits size")
    public void octKeysWithKeyNamePrefixAreCreatedWithBitsSizeAndHSM(
            final int count, final String prefix, final int size) {
        IntStream.range(0, count).forEach(i -> {
            anOctKeyIsCreatedWithNameKeySizeAndHsmSet(prefix + (i + 1), size);
            octKeyCreationRequestIsSent();
        });
    }

    @Given("{int} RSA keys with {name} prefix are created with {int} bits size {hsm} HSM")
    public void rsaKeysWithKeyNamePrefixAreCreatedWithBitsSizeAndHSM(
            final int count, final String prefix, final int size, final boolean hsm) {
        IntStream.range(0, count).forEach(i -> {
            anRsaKeyIsCreatedWithNameKeySizeAndHsmSet(prefix + (i + 1), size, hsm);
            rsaKeyCreationRequestIsSent();
        });
    }

    @Given("{int} keys with {name} prefix are deleted")
    public void countKeysWithKeyNamePrefixAreDeleted(
            final int count, final String prefix) {
        final List<String> deleted = IntStream.range(0, count).mapToObj(i -> {
            final DeletedKey actual = context.getClient().beginDeleteKey(prefix + (i + 1)).waitForCompletion().getValue();
            context.setLastDeleted(actual);
            return actual;
        }).map(DeletedKey::getRecoveryId).collect(Collectors.toList());
        context.setDeletedRecoveryIds(deleted);
    }

    @When("the deleted key properties are listed")
    public void theDeletedKeyPropertiesAreListed() {
        final PagedIterable<DeletedKey> actual = context.getClient().listDeletedKeys();
        final List<String> list = actual.stream()
                .map(DeletedKey::getRecoveryId)
                .collect(Collectors.toList());
        context.setListedIds(list);
    }

    @When("the key is recovered")
    public void theKeyIsRecovered() {
        final DeletedKey deleted = context.getLastDeleted();
        final KeyVaultKey key = context.getClient().beginRecoverDeletedKey(deleted.getName()).waitForCompletion().getValue();
        context.addFetchedKey(key.getName(), key);
    }

    @When("the last version of the key is prepared for an update")
    public void theLastVersionOfTheKeyIsPreparedForAnUpdate() {
        final KeyVaultKey lastResult = context.getLastResult();
        final KeyProperties updatedProperties = context.getClient()
                .getKey(lastResult.getName(), lastResult.getProperties().getVersion()).getProperties();
        context.setUpdateProperties(updatedProperties);
    }

    @When("the key is updated to expire {optionalInt} seconds after creation")
    public void theKeyIsUpdatedToExpireExpiresSecondsAfterCreation(final Integer expire) {
        Optional.ofNullable(expire).ifPresent(e -> context.getUpdateProperties().setExpiresOn(NOW.plusSeconds(e)));
    }

    @When("the key is updated to be not usable until {optionalInt} seconds after creation")
    public void theKeyIsUpdatedToBeNotUsableUntilNotBeforeSecondsAfterCreation(final Integer notBefore) {
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
        final KeyProperties properties = context.getClient()
                .updateKeyProperties(context.getUpdateProperties(), context.getUpdateKeyOperations())
                .getProperties();
        fetchLatestKeyVersion(properties.getName());
    }

    @When("the created key is used to encrypt {clearText} with {algorithm}")
    public void theCreatedKeyIsUsedToEncryptClearTextWithAlgorithm(final byte[] text, final EncryptionAlgorithm algorithm) {
        final String keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId));
        final EncryptResult encryptResult = context.getCryptographyClient()
                .encrypt(encryptParams(algorithm, text), Context.NONE);
        context.setEncryptResult(encryptResult);
    }

    @When("the created key is used to sign {clearText} with {signAlgorithm}")
    public void theCreatedKeyIsUsedToSignClearTextWithAlgorithm(final byte[] text, final SignatureAlgorithm algorithm) {
        final String keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId));
        final byte[] digest = hash(text, algorithm.toString());
        final SignResult signResult = context.getCryptographyClient()
                .sign(algorithm, digest);
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
        final String keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId));
        final DecryptResult decryptResult = context.getCryptographyClient()
                .decrypt(decryptParams(algorithm, context.getEncryptResult()), Context.NONE);
        context.setDecryptResult(decryptResult);
    }

    @And("the encrypted value is decrypted using the original OCT key using {algorithm}")
    public void theEncryptedValueIsDecryptedUsingTheOriginalOctKeyWithAlgorithm(final EncryptionAlgorithm algorithm) throws Exception {
        final Cipher cipher = Cipher.getInstance(getSymmetricalEncryptionAlgName(algorithm), new BouncyCastleProvider());
        final EncryptResult encryptResult = context.getEncryptResult();
        cipher.init(Cipher.DECRYPT_MODE, context.getSecretKey(), new IvParameterSpec(encryptResult.getIv()));
        final byte[] plain = cipher.doFinal(encryptResult.getCipherText());
        context.setDecryptResult(new DecryptResult(plain, algorithm, context.getLastResult().getId()));
    }

    @And("the signature of {clearText} is verified with {signAlgorithm}")
    public void theSignValueIsVerifiedWithAlgorithm(final byte[] text, final SignatureAlgorithm algorithm) {
        final String keyId = context.getLastResult().getKey().getId();
        context.setCryptographyClient(context.getProvider().getCryptoClient(keyId));
        final byte[] digest = hash(text, algorithm.toString());
        final VerifyResult verifyResult = context.getCryptographyClient().verify(algorithm, digest, context.getSignatureResult());
        context.setVerifyResult(verifyResult.isValid());
    }

    @And("the EC signature of {clearText} is verified using the original public key with {signAlgorithm}")
    public void theEcSignValueIsVerifiedUsingOriginalPublicKeyWithAlgorithm(final byte[] text, final SignatureAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        final byte[] digest = hash(text, algorithm.toString());
        final Signature ecVerify = Signature.getInstance(getAsymmetricSignatureAlgorithm(algorithm));
        ecVerify.initVerify(context.getKeyPair().getPublic());
        ecVerify.update(digest);
        final boolean result = ecVerify.verify(context.getSignatureResult());
        context.setVerifyResult(result);
    }

    @And("the RSA signature of {clearText} is verified using the original public key with {signAlgorithm}")
    public void theRsaSignValueIsVerifiedUsingOriginalPublicKeyWithAlgorithm(final byte[] text, final SignatureAlgorithm algorithm)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        final byte[] digest = hash(text, algorithm.toString());
        final Signature rsaVerify = Signature.getInstance(getAsymmetricSignatureAlgorithm(algorithm), new BouncyCastleProvider());
        rsaVerify.initVerify(context.getKeyPair().getPublic());
        rsaVerify.update(digest);
        final boolean result = rsaVerify.verify(context.getSignatureResult());
        context.setVerifyResult(result);
    }

    private byte[] hash(final byte[] text, final String algorithm) {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-" + algorithm.substring(2, 5));
            md.update(text);
            return md.digest();
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
    }

    private byte[] getIv() {
        return "iv-parameter-val".getBytes(StandardCharsets.UTF_8);
    }

    private EncryptParameters encryptParams(final EncryptionAlgorithm encryptionAlgorithm, final byte[] clearText) {
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

    private DecryptParameters decryptParams(final EncryptionAlgorithm encryptionAlgorithm, final EncryptResult encryptResult) {
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
            return "NONEwithECDSAinP1363Format";
        } else if (algorithm == ES256K) {
            return "NONEwithECDSAinP1363Format";
        } else if (algorithm == ES384) {
            return "NONEwithECDSAinP1363Format";
        } else if (algorithm == ES512) {
            return "NONEwithECDSAinP1363Format";
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
}
