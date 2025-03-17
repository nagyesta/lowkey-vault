package com.github.nagyesta.lowkeyvault.testcontainers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.Optional;

public final class KeyStoreMerger implements AutoCloseable {

    /**
     * The standard system property used for defining a custom trust store's location for Java.
     */
    public static final String CUSTOM_TRUST_STORE_LOCATION_PROPERTY = "javax.net.ssl.trustStore";
    /**
     * The system property where we are saving the original value of {@link #CUSTOM_TRUST_STORE_LOCATION_PROPERTY}.
     */
    public static final String BACKUP_TRUST_STORE_LOCATION_PROPERTY = "backup.ssl.trustStore";
    /**
     * The standard system property used for defining a custom trust store's type for Java.
     */
    public static final String CUSTOM_TRUST_STORE_TYPE_PROPERTY = "javax.net.ssl.trustStoreType";
    /**
     * The system property where we are saving the original value of {@link #CUSTOM_TRUST_STORE_TYPE_PROPERTY}.
     */
    public static final String BACKUP_TRUST_STORE_TYPE_PROPERTY = "backup.ssl.trustStoreType";
    /**
     * The standard system property used for defining a custom trust store's password for Java.
     */
    public static final String CUSTOM_TRUST_STORE_PASSWORD_PROPERTY = "javax.net.ssl.trustStorePassword";
    /**
     * The system property where we are saving the original value of {@link #CUSTOM_TRUST_STORE_PASSWORD_PROPERTY}.
     */
    public static final String BACKUP_TRUST_STORE_PASSWORD_PROPERTY = "backup.ssl.trustStorePassword";
    /**
     * The default password of the default Java trust store.
     */
    public static final String DEFAULT_PASSWORD = "changeit";
    private static final String JAVA_HOME = "java.home";
    private static final String LIB = "lib";
    private static final String SECURITY = "security";
    private static final String JSSECACERTS = "jssecacerts";
    private static final String CACERTS = "cacerts";

    private final KeyStore lowkeyVaultStore;
    private final char[] lowkeyVaultStorePassword;

    public KeyStoreMerger(
            final KeyStore lowkeyVaultStore,
            final char[] lowkeyVaultStorePassword) {
        this.lowkeyVaultStore = lowkeyVaultStore;
        this.lowkeyVaultStorePassword = lowkeyVaultStorePassword;
    }

    public boolean shouldMerge() {
        return System.getProperty(BACKUP_TRUST_STORE_LOCATION_PROPERTY) == null;
    }

    public void mergeDefaultTrustStore() {
        if (!shouldMerge()) {
            throw new IllegalStateException("Trust store is already merged: "
                    + System.getProperty(CUSTOM_TRUST_STORE_LOCATION_PROPERTY));
        }

        final var originalLocation = findTrustStore();
        final var originalStoreType = findTrustStoreType();
        final var originalPassword = findTrustStorePassword();
        final var originalTrustStore = loadOriginal(originalLocation, originalStoreType, originalPassword.toCharArray());
        doMerge(originalTrustStore, originalPassword.toCharArray());
        final var tempFile = storeToTempFile(originalTrustStore, originalPassword.toCharArray());
        System.setProperty(BACKUP_TRUST_STORE_LOCATION_PROPERTY, originalLocation.toString());
        System.setProperty(BACKUP_TRUST_STORE_TYPE_PROPERTY, originalStoreType);
        System.setProperty(BACKUP_TRUST_STORE_PASSWORD_PROPERTY, originalPassword);
        System.setProperty(CUSTOM_TRUST_STORE_LOCATION_PROPERTY, tempFile.toString());
        System.setProperty(CUSTOM_TRUST_STORE_TYPE_PROPERTY, originalStoreType);
        System.setProperty(CUSTOM_TRUST_STORE_PASSWORD_PROPERTY, originalPassword);
    }

    void doMerge(
            final KeyStore toStore,
            final char[] toStorePassword) {
        try {
            final var fromProtectionParam = new KeyStore.PasswordProtection(lowkeyVaultStorePassword);
            final var toProtectionParam = new KeyStore.PasswordProtection(toStorePassword);
            lowkeyVaultStore.aliases().asIterator().forEachRemaining(
                    alias -> {
                        try {
                            if (toStore.containsAlias(alias)) {
                                return;
                            }
                            final var entry = lowkeyVaultStore.getEntry(alias, fromProtectionParam);
                            toStore.setEntry(alias, entry, toProtectionParam);
                        } catch (final Exception e) {
                            throw new IllegalStateException(e);
                        }
                    }
            );
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    String findTrustStoreType() {
        return Optional.ofNullable(System.getProperty(CUSTOM_TRUST_STORE_TYPE_PROPERTY))
                .orElse(KeyStore.getDefaultType());
    }

    String findTrustStorePassword() {
        return Optional.ofNullable(System.getProperty(CUSTOM_TRUST_STORE_PASSWORD_PROPERTY))
                .orElse(DEFAULT_PASSWORD);
    }

    Path findTrustStore() {
        return Optional.ofNullable(System.getProperty(CUSTOM_TRUST_STORE_LOCATION_PROPERTY))
                .map(Path::of)
                .map(Path::toAbsolutePath)
                .filter(Files::exists)
                .or(() -> Optional.of(Paths.get(System.getProperty(JAVA_HOME), LIB, SECURITY, JSSECACERTS)))
                .map(Path::toAbsolutePath)
                .filter(Files::exists)
                .or(() -> Optional.of(Paths.get(System.getProperty(JAVA_HOME), LIB, SECURITY, CACERTS)))
                .map(Path::toAbsolutePath)
                .filter(Files::exists)
                .orElseThrow(() -> new IllegalStateException("Unable to find original trust store"));
    }

    Path storeToTempFile(
            final KeyStore originalTrustStore, final char[] storePassword) {
        try {
            final var tempFile = Files.createTempFile("lowkey-vault-trust-store-", ".keystore");
            originalTrustStore.store(Files.newOutputStream(tempFile), storePassword);
            tempFile.toFile().deleteOnExit();
            return tempFile;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private KeyStore loadOriginal(final Path storeLocation, final String storeType, final char[] storePassword) {
        try {
            final var trustStore = KeyStore.getInstance(storeType);
            trustStore.load(Files.newInputStream(storeLocation), storePassword);
            return trustStore;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        restoreFromBackup(BACKUP_TRUST_STORE_LOCATION_PROPERTY, CUSTOM_TRUST_STORE_LOCATION_PROPERTY);
        restoreFromBackup(BACKUP_TRUST_STORE_TYPE_PROPERTY, CUSTOM_TRUST_STORE_TYPE_PROPERTY);
        restoreFromBackup(BACKUP_TRUST_STORE_PASSWORD_PROPERTY, CUSTOM_TRUST_STORE_PASSWORD_PROPERTY);
    }

    private void restoreFromBackup(final String backup, final String original) {
        final var backupLocation = System.getProperty(backup);
        if (backupLocation != null) {
            System.setProperty(original, backupLocation);
            System.clearProperty(backup);
        }
    }
}
