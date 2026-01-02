package com.github.nagyesta.lowkeyvault.context;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

class OptionalQueryApiVersionResolverTest {

    public static Stream<Arguments> versionProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("api-version=2021-10-01", true, "2021-10-01"))
                .add(Arguments.of("api-version=2021-10-01", false, null))
                .add(Arguments.of(null, true, null))
                .add(Arguments.of("", true, null))
                .add(Arguments.of("api-version=2021-10-01&other=value", true, "2021-10-01"))
                .add(Arguments.of("api-version=2021-10-01&other=value", false, null))
                .add(Arguments.of("other=value", true, null))
                .add(Arguments.of("other=value", false, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("versionProvider")
    void testResolveVersionShouldReturnVersionWhenTheRequestIsSecureAndTheParameterIsPresent(
            final String queryString,
            final boolean secure,
            final String expected) {
        // given
        final var underTest = new OptionalQueryApiVersionResolver();
        final var request = mock(HttpServletRequest.class);
        when(request.isSecure()).thenReturn(secure);
        when(request.getQueryString()).thenReturn(queryString);

        // when
        final var actual = underTest.resolveVersion(request);

        // then
        verify(request).isSecure();
        if (secure) {
            verify(request).getQueryString();
        } else {
            verify(request, never()).getQueryString();
        }
        if (expected == null) {
            assertNull(actual);
        } else {
            assertEquals(expected, actual);
        }
    }
}
