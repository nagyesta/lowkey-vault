package com.github.nagyesta.lowkeyvault.keys;

import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.*;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.ClientProviderConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;
import java.util.TreeMap;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class KeyCreateTest extends BaseKeyTest {

    @Autowired
    public KeyCreateTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> validRsaKeyProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("rsaKey", 2048, 257))
                .add(Arguments.of("rsaKey4096", 4096, 513))
                .add(Arguments.of("rsa-key-name", 2048, 257))
                .add(Arguments.of("rsa-key-name-4096", 4096, 513))
                .build();
    }

    public static Stream<Arguments> validEcKeyProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("ecKey256", KeyCurveName.P_256, 32))
                .add(Arguments.of("ecKey256k", KeyCurveName.P_256K, 32))
                .add(Arguments.of("ecKey384", KeyCurveName.P_384, 48))
                .add(Arguments.of("ecKey521", KeyCurveName.P_521, 65))
                .add(Arguments.of("ec-key-name-256", KeyCurveName.P_256, 32))
                .add(Arguments.of("ec-key-name-256k", KeyCurveName.P_256K, 32))
                .add(Arguments.of("ec-key-name-384", KeyCurveName.P_384, 48))
                .add(Arguments.of("ec-key-name-521", KeyCurveName.P_521, 65))
                .build();
    }

    public static Stream<Arguments> validOctKeyProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("octKey128", 128, 16))
                .add(Arguments.of("octKey192", 192, 16))
                .add(Arguments.of("octKey256", 256, 16))
                .add(Arguments.of("oct-key-name-128", 128, 16))
                .add(Arguments.of("oct-key-name-192", 192, 16))
                .add(Arguments.of("oct-key-name-256", 256, 16))
                .build();
    }

    @ParameterizedTest
    @MethodSource("keyTypeProvider")
    @Tags({@Tag("create")})
    void testCreateKeyShouldPopulateOptionalFieldsWhenValuesProvidedASync(final KeyType keyType) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();

        final CreateKeyOptions createKeyOptions = new CreateKeyOptions(name, keyType);
        createKeyOptions.setEnabled(false);
        createKeyOptions.setKeyOperations(OPERATION);
        createKeyOptions.setNotBefore(NOW);
        createKeyOptions.setExpiresOn(IN_AN_HOUR);
        createKeyOptions.setTags(TAG_MAP);

        //when
        final KeyVaultKey actual = keyClient.createKey(createKeyOptions).block();

        //then
        assertMatchesExpectations(keyType, name, actual);
    }

    @ParameterizedTest
    @MethodSource("keyTypeProvider")
    @Tags({@Tag("create")})
    void testCreateKeyShouldPopulateOptionalFieldsWhenValuesProvidedSync(final KeyType keyType) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();

        final CreateKeyOptions createKeyOptions = new CreateKeyOptions(name, keyType);
        createKeyOptions.setEnabled(false);
        createKeyOptions.setKeyOperations(OPERATION);
        createKeyOptions.setNotBefore(NOW);
        createKeyOptions.setExpiresOn(IN_AN_HOUR);
        createKeyOptions.setTags(TAG_MAP);

        //when
        final KeyVaultKey actual = keyClient.createKey(createKeyOptions);

        //then
        assertMatchesExpectations(keyType, name, actual);
    }

    @ParameterizedTest
    @MethodSource("validRsaKeyProvider")
    @Tags({@Tag("rsa"), @Tag("create")})
    void testNewRsaKeyShouldBeCreatedWhenItDoesNotExistYetSync(final String keyName, final int keySize, final int byteArrayLength) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = keyName + SYNC;

        //when
        final KeyVaultKey actual = keyClient
                .createRsaKey(new CreateRsaKeyOptions(name).setKeySize(keySize));

        //then
        assertRsaKey(name, byteArrayLength, actual);
    }

    @ParameterizedTest
    @MethodSource("validRsaKeyProvider")
    @Tags({@Tag("rsa"), @Tag("create")})
    void testNewRsaKeyShouldBeCreatedWhenItDoesNotExistYetAsync(final String keyName, final int keySize, final int byteArrayLength) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = keyName + ASYNC;

        //when
        final KeyVaultKey actual = keyClient
                .createRsaKey(new CreateRsaKeyOptions(name).setKeySize(keySize))
                .block();

        //then
        assertRsaKey(name, byteArrayLength, actual);
    }

    @ParameterizedTest
    @MethodSource("validEcKeyProvider")
    @Tags({@Tag("ec"), @Tag("create")})
    void testNewEcKeyShouldBeCreatedWhenItDoesNotExistYetSync(final String keyName, final KeyCurveName keyCurveName, final int byteArrayLength) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = keyName + SYNC;

        //when
        final KeyVaultKey actual = keyClient
                .createEcKey(new CreateEcKeyOptions(name).setCurveName(keyCurveName));

        //then
        assertEcKey(name, keyCurveName, byteArrayLength, actual);
    }

    @ParameterizedTest
    @MethodSource("validEcKeyProvider")
    @Tags({@Tag("ec"), @Tag("create")})
    void testNewEcKeyShouldBeCreatedWhenItDoesNotExistYetAsync(final String keyName, final KeyCurveName keyCurveName, final int byteArrayLength) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = keyName + ASYNC;

        //when
        final KeyVaultKey actual = keyClient
                .createEcKey(new CreateEcKeyOptions(name).setCurveName(keyCurveName))
                .block();

        //then
        assertEcKey(name, keyCurveName, byteArrayLength, actual);
    }

    @ParameterizedTest
    @MethodSource("validOctKeyProvider")
    @Tags({@Tag("oct"), @Tag("create")})
    void testNewOctKeyShouldBeCreatedWhenItDoesNotExistYetSync(final String keyName, final int keySize, final int byteArrayLength) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = keyName + SYNC;

        //when
        final KeyVaultKey actual = keyClient
                .createOctKey(new CreateOctKeyOptions(name).setKeySize(keySize));

        //then
        assertOctKey(name, byteArrayLength, actual);
    }

    @ParameterizedTest
    @MethodSource("validOctKeyProvider")
    @Tags({@Tag("oct"), @Tag("create")})
    void testNewOctKeyShouldBeCreatedWhenItDoesNotExistYetAsync(final String keyName, final int keySize, final int byteArrayLength) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = keyName + ASYNC;

        //when
        final KeyVaultKey actual = keyClient
                .createOctKey(new CreateOctKeyOptions(name).setKeySize(keySize))
                .block();

        //then
        assertOctKey(name, byteArrayLength, actual);
    }

    private void assertMatchesExpectations(final KeyType keyType, final String name, final KeyVaultKey actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyType, actual.getKeyType());
        Assertions.assertEquals(name, actual.getName());
        Assertions.assertIterableEquals(Collections.singleton(OPERATION), actual.getKeyOperations());
        Assertions.assertIterableEquals(new TreeMap<>(TAG_MAP).entrySet(), new TreeMap<>(actual.getProperties().getTags()).entrySet());
        Assertions.assertEquals(NOW, actual.getProperties().getNotBefore());
        Assertions.assertEquals(IN_AN_HOUR, actual.getProperties().getExpiresOn());
        Assertions.assertTrue(NOW.isBefore(actual.getProperties().getCreatedOn()));
        Assertions.assertTrue(NOW.isBefore(actual.getProperties().getUpdatedOn()));
    }

    private void assertRsaKey(final String keyName, final int byteArrayLength, final KeyVaultKey actual) {
        assertCommonFields(keyName, KeyType.RSA, actual);
        assertRsaFields(byteArrayLength, actual);
        assertEcFieldsMissing(actual);
        assertOctFieldsMissing(actual);
    }

    private void assertEcKey(final String keyName, final KeyCurveName keyCurveName, final int byteArrayLength, final KeyVaultKey actual) {
        assertCommonFields(keyName, KeyType.EC, actual);
        assertRsaFieldsMissing(actual);
        assertEcFields(keyCurveName, byteArrayLength, actual);
        assertOctFieldsMissing(actual);
    }

    private void assertOctKey(final String keyName, final int byteArrayLength, final KeyVaultKey actual) {
        assertCommonFields(keyName, KeyType.OCT, actual);
        assertRsaFieldsMissing(actual);
        assertEcFieldsMissing(actual);
        assertOctFields(byteArrayLength, actual);
    }

    private void assertCommonFields(final String keyName, final KeyType keyType, final KeyVaultKey actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(keyName, actual.getName());
        Assertions.assertTrue(actual.getId().startsWith(provider.getVaultUrl()));
        Assertions.assertTrue(actual.getId().contains(keyName));
        Assertions.assertEquals(keyType, actual.getKeyType());
        Assertions.assertEquals(keyType, actual.getKey().getKeyType());
    }

    private void assertRsaFields(final int byteArrayLength, final KeyVaultKey actual) {
        assertByteArrayLength(byteArrayLength, actual.getKey().getN());
        Assertions.assertNotNull(actual.getKey().getE());
    }

    private void assertRsaFieldsMissing(final KeyVaultKey actual) {
        Assertions.assertNull(actual.getKey().getN());
        Assertions.assertNull(actual.getKey().getE());
    }

    private void assertEcFields(final KeyCurveName keyCurveName, final int byteArrayLength, final KeyVaultKey actual) {
        Assertions.assertEquals(keyCurveName, actual.getKey().getCurveName());
        assertByteArrayLength(byteArrayLength, actual.getKey().getX());
        assertByteArrayLength(byteArrayLength, actual.getKey().getY());
    }

    private void assertEcFieldsMissing(final KeyVaultKey actual) {
        Assertions.assertNull(actual.getKey().getCurveName());
        Assertions.assertNull(actual.getKey().getX());
        Assertions.assertNull(actual.getKey().getY());
    }

    private void assertOctFields(final int byteArrayLength, final KeyVaultKey actual) {
        assertByteArrayLength(byteArrayLength, actual.getKey().getK());
    }

    private void assertOctFieldsMissing(final KeyVaultKey actual) {
        Assertions.assertNull(actual.getKey().getK());
    }

    private void assertByteArrayLength(final int byteArrayLength, final byte[] bytes) {
        Assertions.assertNotNull(bytes);
        Assertions.assertTrue(byteArrayLength - 1 <= bytes.length && byteArrayLength + 1 >= bytes.length,
                "Byte array was " + bytes.length + " long, expected " + byteArrayLength + " (+/-1 tolerance)");
    }
}
