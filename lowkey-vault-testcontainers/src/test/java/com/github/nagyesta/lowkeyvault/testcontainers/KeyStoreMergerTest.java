package com.github.nagyesta.lowkeyvault.testcontainers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.github.nagyesta.lowkeyvault.testcontainers.KeyStoreMerger.DEFAULT_PASSWORD;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("resource")
@Isolated
class KeyStoreMergerTest {

    @Test
    void testShouldMergeShouldReturnTrueWhenBackupIsNotPresent() {
        //given
        System.clearProperty(KeyStoreMerger.BACKUP_TRUST_STORE_LOCATION_PROPERTY);
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.shouldMerge();

        //then
        assertTrue(actual, "When the backup store is not present, we should have merged.");
    }

    @Test
    void testShouldMergeShouldReturnFalseWhenBackupIsPresent() {
        //given
        System.setProperty(KeyStoreMerger.BACKUP_TRUST_STORE_LOCATION_PROPERTY, "-");
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.shouldMerge();

        //then
        assertFalse(actual, "When the backup store is present, we should not merge.");
    }

    @Test
    void testFindTrustStoreShouldReturnTheCustomStorePathWhenItIsSetAndTheFileExists() {
        //given
        final var customCertPath = Objects.requireNonNull(getClass().getResource("/cert.jks")).getFile();
        System.setProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_LOCATION_PROPERTY, customCertPath);
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.findTrustStore();

        //then
        assertEquals(Path.of(customCertPath), actual);
    }

    @Test
    void testFindTrustStoreShouldReturnTheDefaultStorePathWhenNoCustomPathIsSet() {
        //given
        System.clearProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_LOCATION_PROPERTY);
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.findTrustStore();

        //then
        assertTrue(actual.endsWith("cacerts"));
    }

    @Test
    void testFindTrustStorePasswordShouldReturnTheCustomStorePasswordWhenItIsSet() {
        //given
        final var expected = "expected";
        System.setProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_PASSWORD_PROPERTY, expected);
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.findTrustStorePassword();

        //then
        assertEquals(expected, actual);
    }

    @Test
    void testFindTrustStorePasswordShouldReturnTheDefaultStorePasswordWhenNoCustomPasswordIsSet() {
        //given
        System.clearProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_PASSWORD_PROPERTY);
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.findTrustStorePassword();

        //then
        assertEquals(DEFAULT_PASSWORD, actual);
    }

    @Test
    void testFindTrustStoreTypeShouldReturnTheCustomStoreTypeWhenItIsSet() {
        //given
        final var expected = "expected";
        System.setProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_TYPE_PROPERTY, expected);
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.findTrustStoreType();

        //then
        assertEquals(expected, actual);
    }

    @Test
    void testFindTrustStoreTypeShouldReturnTheDefaultStoreTypeWhenNoCustomTypeIsSet() {
        //given
        System.clearProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_TYPE_PROPERTY);
        final var underTest = new KeyStoreMerger(null, null);

        //when
        final var actual = underTest.findTrustStoreType();

        //then
        assertEquals(KeyStore.getDefaultType(), actual);
    }

    @Test
    void testMergeDefaultTrustStoreShouldUseTheOriginalFormatAndAddLowkeyVaultCertsWhenCalled() {
        //given
        final var lowkeyVaultKeyStore = loadLowkeyVaultKeyStore();
        final var customCertPath = Objects.requireNonNull(getClass().getResource("/cert.jks")).getFile();
        System.setProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_LOCATION_PROPERTY, customCertPath);
        final var password = "password";
        System.setProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_PASSWORD_PROPERTY, password);
        System.setProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_TYPE_PROPERTY, StoreType.JKS.name());
        final var underTest = new KeyStoreMerger(lowkeyVaultKeyStore, DEFAULT_PASSWORD.toCharArray());

        //when
        underTest.mergeDefaultTrustStore();

        //then
        final var actualLocation = System.getProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_LOCATION_PROPERTY);
        final var actualPassword = System.getProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_PASSWORD_PROPERTY);
        final var actualType = System.getProperty(KeyStoreMerger.CUSTOM_TRUST_STORE_TYPE_PROPERTY);
        final var backupLocation = System.getProperty(KeyStoreMerger.BACKUP_TRUST_STORE_LOCATION_PROPERTY);
        final var backupPassword = System.getProperty(KeyStoreMerger.BACKUP_TRUST_STORE_PASSWORD_PROPERTY);
        final var backupType = System.getProperty(KeyStoreMerger.BACKUP_TRUST_STORE_TYPE_PROPERTY);
        final var actualKeyStore = loadKeyStore(Path.of(actualLocation), StoreType.JKS.name(), password.toCharArray());
        final var originalKeyStore = loadKeyStore(Path.of(backupLocation), StoreType.JKS.name(), password.toCharArray());

        assertNotEquals(customCertPath, actualLocation);
        assertEquals(password, actualPassword);
        assertEquals(StoreType.JKS.name(), actualType);
        assertEquals(customCertPath, backupLocation);
        assertEquals(password, backupPassword);
        assertEquals(StoreType.JKS.name(), backupType);
        final var actual = toMap(actualKeyStore, password);
        final var original = toMap(originalKeyStore, password);
        final var lowkeyVault = toMap(lowkeyVaultKeyStore, DEFAULT_PASSWORD);
        final Map<String, Object> expectedMerged = new TreeMap<>();
        expectedMerged.putAll(original);
        expectedMerged.putAll(lowkeyVault);
        assertIterableEquals(expectedMerged.entrySet(), actual.entrySet());
    }

    private static Map<String, Object> toMap(
            final KeyStore actualKeyStore,
            final String password) {
        final Map<String, Object> actual = new TreeMap<>();
        try {
            actualKeyStore.aliases().asIterator().forEachRemaining(alias -> {
                try {
                    actual.put(alias + "_key", actualKeyStore.getKey(alias, password.toCharArray()));
                    actual.put(alias + "_cert", actualKeyStore.getCertificate(alias));
                } catch (final Exception ignore) {
                    //ignored
                }
            });
        } catch (final Exception ignore) {
            //ignored
        }
        return actual;
    }

    private KeyStore loadLowkeyVaultKeyStore() {
        final var lowkeyVaultCertPath = Objects.requireNonNull(getClass().getResource("/lowkey-vault-keystore.p12")).getFile();
        return loadKeyStore(Path.of(lowkeyVaultCertPath), StoreType.PKCS12.name(), DEFAULT_PASSWORD.toCharArray());
    }

    private static KeyStore loadKeyStore(
            final Path storePath,
            final String type,
            final char[] password) {
        try {
            final var trustStore = KeyStore.getInstance(type);
            trustStore.load(Files.newInputStream(storePath), password);
            return trustStore;
        } catch (final Exception e) {
            fail("Failed to load default Lowkey Vault keyStore", e);
            return null;
        }
    }
}
