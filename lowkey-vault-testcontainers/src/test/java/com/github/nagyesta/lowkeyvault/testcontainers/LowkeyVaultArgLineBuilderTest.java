package com.github.nagyesta.lowkeyvault.testcontainers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

class LowkeyVaultArgLineBuilderTest {

    public static Stream<Arguments> vaultNameProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(Set.of("i n v a l i d")))
                .add(Arguments.of((Set<String>) null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("vaultNameProvider")
    void testVaultNamesShouldThrowExceptionWhenCalledWithInvalidData(final Set<String> value) {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.vaultNames(value));

        //then + exception
    }

    @ParameterizedTest
    @ValueSource(strings = {"-", "valid"})
    void testVaultNamesShouldSetArgumentWhenCalledWithValidData(final String name) {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_VAULT_NAMES=" + name);

        //when
        final var actual = underTest.vaultNames(Set.of(name)).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testLogicalHostShouldNotSetValueWhenCalledWithNull() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.logicalHost(null).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testLogicalHostShouldSetArgumentWhenCalledWithValidData() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_IMPORT_TEMPLATE_HOST=127.0.0.1");

        //when
        final var actual = underTest.logicalHost("127.0.0.1").build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testLogicalPortShouldNotSetValueWhenCalledWithNull() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.logicalPort(null).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testLogicalPortShouldSetArgumentWhenCalledWithValidData() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_IMPORT_TEMPLATE_PORT=1");

        //when
        final var actual = underTest.logicalPort(1).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testDebugShouldNotSetValueWhenCalledWithFalse() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.debug(false).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testDebugShouldSetArgumentWhenCalledWithTrue() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_DEBUG_REQUEST_LOG=true");

        //when
        final var actual = underTest.debug(true).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    @SuppressWarnings({"deprecation", "removal"})
    void testImportFileShouldSetArgumentWhenCalledWithFile() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_IMPORT_LOCATION=/import/vaults.json");

        //when
        final var actual = underTest.importFile(new File(".")).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testImportFileShouldSetArgumentWhenCalledWithString() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_IMPORT_LOCATION=/import/import.json");

        //when
        final var actual = underTest.importFile("/import/import.json").build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testImportFileShouldNotSetArgumentWhenCalledWithNullString() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.importFile((String) null).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testUsePersistenceShouldSetArgumentWhenCalledWithString() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_IMPORT_LOCATION=/import/vaults.json",
                "--LOWKEY_EXPORT_LOCATION=/import/vaults.json");

        //when
        final var actual = underTest.usePersistence("/import/vaults.json").build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testUsePersistenceShouldNotSetArgumentWhenCalledWithNullString() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.usePersistence(null).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testCustomSslCertificateShouldSetArgumentWhenCalledWithValidFile() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--server.ssl.key-store=/config/cert.store",
                "--server.ssl.key-store-type=JKS",
                "--server.ssl.key-store-password=pass");

        //when
        final var actual = underTest.customSSLCertificate(new File("."), "pass", StoreType.JKS).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testCustomSslCertificateShouldNotSetArgumentWhenCalledWithNull() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.customSSLCertificate(null, "pass", StoreType.JKS).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testAliasesShouldSetArgumentWhenCalledWithValidMap() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final Map<String, Set<String>> aliases = new TreeMap<>(Map.of(
                "localhost", new TreeSet<>(Set.of("alias.localhost", "alias.localhost:<port>")),
                "lowkey-vault", Set.of("alias.localhost:30443")));
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                "--LOWKEY_VAULT_ALIASES="
                        + "localhost=alias.localhost,"
                        + "localhost=alias.localhost:<port>,"
                        + "lowkey-vault=alias.localhost:30443"
        );

        //when
        final var actual = underTest.aliases(aliases).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testAliasesShouldNotSetArgumentWhenCalledWithNullOrEmptyMap(final Map<String, Set<String>> map) {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.aliases(map).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @Test
    void testAdditionalArgsShouldSetArgumentWhenCalledWithValidMap() {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var additional = "--LOWKEY_VAULT_ALIASES=\""
                + "localhost=alias.localhost,"
                + "localhost=alias.localhost:<port>,"
                + "lowkey-vault=alias.localhost:30443"
                + "\"";
        final var expected = List.of(
                "--LOWKEY_VAULT_RELAXED_PORTS=true",
                additional);

        //when
        final var actual = underTest.additionalArgs(List.of(additional)).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testAdditionalArgsShouldNotSetArgumentWhenCalledWithNullOrEmptyMap(final List<String> list) {
        //given
        final var underTest = new LowkeyVaultArgLineBuilder();
        final var expected = List.of("--LOWKEY_VAULT_RELAXED_PORTS=true");

        //when
        final var actual = underTest.additionalArgs(list).build();

        //then
        Assertions.assertIterableEquals(expected, actual);
    }
}
