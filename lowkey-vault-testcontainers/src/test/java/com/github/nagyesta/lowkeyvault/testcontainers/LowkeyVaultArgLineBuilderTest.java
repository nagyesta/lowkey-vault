package com.github.nagyesta.lowkeyvault.testcontainers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.List;
import java.util.Set;
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
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.vaultNames(value));

        //then + exception
    }

    @ParameterizedTest
    @ValueSource(strings = {"-", "valid"})
    void testVaultNamesShouldSetArgumentWhenCalledWithValidData(final String name) {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.vaultNames(Set.of(name)).build();

        //then
        Assertions.assertIterableEquals(List.of("--LOWKEY_VAULT_NAMES=" + name), actual);
    }

    @Test
    void testLogicalHostShouldNotSetValueWhenCalledWithNull() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.logicalHost(null).build();

        //then
        Assertions.assertIterableEquals(List.of(), actual);
    }

    @Test
    void testLogicalHostShouldSetArgumentWhenCalledWithValidData() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.logicalHost("127.0.0.1").build();

        //then
        Assertions.assertIterableEquals(List.of("--LOWKEY_IMPORT_TEMPLATE_HOST=127.0.0.1"), actual);
    }

    @Test
    void testLogicalPortShouldNotSetValueWhenCalledWithNull() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.logicalPort(null).build();

        //then
        Assertions.assertIterableEquals(List.of(), actual);
    }

    @Test
    void testLogicalPortShouldSetArgumentWhenCalledWithValidData() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.logicalPort(1).build();

        //then
        Assertions.assertIterableEquals(List.of("--LOWKEY_IMPORT_TEMPLATE_PORT=1"), actual);
    }

    @Test
    void testDebugShouldNotSetValueWhenCalledWithFalse() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.debug(false).build();

        //then
        Assertions.assertIterableEquals(List.of(), actual);
    }

    @Test
    void testDebugShouldSetArgumentWhenCalledWithTrue() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.debug(true).build();

        //then
        Assertions.assertIterableEquals(List.of("--LOWKEY_DEBUG_REQUEST_LOG=true"), actual);
    }

    @Test
    void testImportFileShouldSetArgumentWhenCalledWithFile() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.importFile(new File(".")).build();

        //then
        Assertions.assertIterableEquals(List.of("--LOWKEY_IMPORT_LOCATION=/import/vaults.json"), actual);
    }

    @Test
    void testImportFileShouldNotSetArgumentWhenCalledWithNull() {
        //given
        final LowkeyVaultArgLineBuilder underTest = new LowkeyVaultArgLineBuilder();

        //when
        final List<String> actual = underTest.importFile(null).build();

        //then
        Assertions.assertIterableEquals(List.of(), actual);
    }
}
