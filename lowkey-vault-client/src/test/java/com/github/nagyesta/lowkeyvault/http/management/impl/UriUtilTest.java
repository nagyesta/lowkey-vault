package com.github.nagyesta.lowkeyvault.http.management.impl;

import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.stream.Stream;

class UriUtilTest {

    private static final String HTTP_LOCALHOST = "http://localhost";
    private static final String HTTPS_LOCALHOST = "https://localhost";
    private static final String PATH = "/path";
    private static final String ALTERNATIVE = "/alternative";
    private static final String HTTPS_LOCALHOST_PATH = HTTPS_LOCALHOST + PATH;
    private static final String HTTP_LOCALHOST_ALTERNATIVE = HTTP_LOCALHOST + ALTERNATIVE;
    private static final String PARAM_1 = "param1";
    private static final String VALUE_1 = "value1";
    private static final String HTTP_LOCALHOST_ALTERNATIVE_PARAM1_VALUE1 = HTTP_LOCALHOST + ALTERNATIVE + "?" + PARAM_1 + "=" + VALUE_1;

    public static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(HTTPS_LOCALHOST, PATH, Map.of(), HTTPS_LOCALHOST_PATH))
                .add(Arguments.of(HTTP_LOCALHOST, ALTERNATIVE, Map.of(), HTTP_LOCALHOST_ALTERNATIVE))
                .add(Arguments.of(HTTP_LOCALHOST, ALTERNATIVE, Map.of(PARAM_1, VALUE_1), HTTP_LOCALHOST_ALTERNATIVE_PARAM1_VALUE1))
                .build();
    }

    public static Stream<Arguments> nullSource() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(HTTP_LOCALHOST, null))
                .add(Arguments.of(null, PATH))
                .build();
    }


    @Test
    void testConstructorShouldThrowExceptionWhenCalled() throws NoSuchMethodException {
        //given
        final var constructor = UriUtil.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        //when
        Assertions.assertThrows(InvocationTargetException.class, constructor::newInstance);

        //then + exception
    }
    @Test
    void testUriBuilderForPathShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(LowkeyVaultException.class, () -> UriUtil.uriBuilderForPath("://localhost", PATH));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("nullSource")
    void testUriBuilderForPathShouldThrowExceptionWhenCalledWithMalformedUri(final String baseUri, final String path) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> UriUtil.uriBuilderForPath(baseUri, path));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testUriBuilderForPathShouldReturnUriWhenCalledWithValidInput(
            final String baseUri, final String path, final Map<String, String> parameters, final String expected) {
        //given

        //when
        final var actual = UriUtil.uriBuilderForPath(baseUri, path, parameters);

        //then
        Assertions.assertEquals(expected, actual.toString());
    }
}
