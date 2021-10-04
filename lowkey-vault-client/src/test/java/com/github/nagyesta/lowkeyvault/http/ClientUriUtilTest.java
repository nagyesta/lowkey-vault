package com.github.nagyesta.lowkeyvault.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

class ClientUriUtilTest {

    public static Stream<Arguments> valueProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of("https://localhost:4433", "https://localhost___4433"))
                .add(Arguments.of("https://localhost:4433", "https://localhost___4433"))
                .add(Arguments.of("https://localhost:8443", "https://localhost___8443"))
                .add(Arguments.of("https://localhost", "https://localhost"))
                .add(Arguments.of("https://localhost:4433/path", "https://localhost___4433/path"))
                .add(Arguments.of("https://localhost:8443/a/path", "https://localhost___8443/a/path"))
                .add(Arguments.of("https://localhost/a/path", "https://localhost/a/path"))
                .add(Arguments.of("https://localhost/a___/path", "https://localhost/a___/path"))
                .add(Arguments.of("https://localhost:8443/___a/path", "https://localhost___8443/___a/path"))
                .build();
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testRevertPortHackShouldReplacePatternWithPortWhenCalled(final String port, final String hack) {
        //given
        final URL input = nullSafeUrl(hack);
        final URL expected = nullSafeUrl(port);

        //when
        final URL actual = ClientUriUtil.revertPortHack(input);

        //then
        Assertions.assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("valueProvider")
    void testHackPortShouldReplacePortWithPatternWhenCalled(final String port, final String hack) {
        //given

        //when
        final String actual = ClientUriUtil.hackPort(port);

        //then
        Assertions.assertEquals(hack, actual);
    }

    private URL nullSafeUrl(final String optionalUrl) {
        return Optional.ofNullable(optionalUrl)
                .map(spec -> Assertions.assertDoesNotThrow(() -> new URL(spec)))
                .orElse(null);
    }
}
