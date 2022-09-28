package com.github.nagyesta.lowkeyvault.context.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.stream.Stream;

class VaultUriUtilTest {

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> validSource() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("localhost", 443, URI.create("https://localhost")))
                .add(Arguments.of("localhost", 8443, URI.create("https://localhost:8443")))
                .add(Arguments.of("localhost", 8444, URI.create("https://localhost:8444")))
                .add(Arguments.of("lowkey-vault.local", 443, URI.create("https://lowkey-vault.local")))
                .add(Arguments.of("lowkey-vault.local", 8080, URI.create("https://lowkey-vault.local:8080")))
                .add(Arguments.of("lowkey-vault.local", 8443, URI.create("https://lowkey-vault.local:8443")))
                .build();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> aliasSource() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("localhost", 8443, URI.create("https://localhost")))
                .add(Arguments.of("localhost:443", 8443, URI.create("https://localhost")))
                .add(Arguments.of("localhost:8443", 8444, URI.create("https://localhost:8443")))
                .add(Arguments.of("localhost:<port>", 8444, URI.create("https://localhost:8444")))
                .add(Arguments.of("lowkey-vault.local:<port>", 443, URI.create("https://lowkey-vault.local")))
                .add(Arguments.of("lowkey-vault.local", 8080, URI.create("https://lowkey-vault.local")))
                .build();
    }

    public static Stream<Arguments> authorityProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("https://localhost"))
                .add(Arguments.of("localhost<port>:8443"))
                .add(Arguments.of("  localhost:8443 "))
                .build();
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final Constructor<VaultUriUtil> constructor = VaultUriUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validSource")
    void testVaultUriShouldOmitPortNumberWhenCalledWithDefault(final String host, final int port, final URI expectedUri) {
        //given

        //when
        final URI actual = VaultUriUtil.vaultUri(host, port);

        //then
        Assertions.assertEquals(expectedUri, actual);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testVaultUriShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> VaultUriUtil.vaultUri(null, 1));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("aliasSource")
    void testAliasUriShouldReplacePortNumberWhenInputContainsPlaceholder(
            final String authority, final int serverPort, final URI expectedUri) {
        //given

        //when
        final URI actual = VaultUriUtil.aliasUri(authority, serverPort);

        //then
        Assertions.assertEquals(expectedUri, actual);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("authorityProvider")
    void testAliasUriShouldThrowExceptionWhenCalledWithInvalidAuthority(final String authority) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> VaultUriUtil.aliasUri(authority, 1));

        //then + exception
    }
}
