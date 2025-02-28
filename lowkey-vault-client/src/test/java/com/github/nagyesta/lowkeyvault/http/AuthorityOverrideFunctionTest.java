package com.github.nagyesta.lowkeyvault.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

class AuthorityOverrideFunctionTest {

    private static final String HTTPS = "https://";
    private static final String ALTERNATIVE_LOCALHOST = "alternative.localhost";
    private static final String ALTERNATIVE_LOCALHOST_30443 = ALTERNATIVE_LOCALHOST + ":8443";
    private static final String HTTPS_ALTERNATIVE_LOCALHOST_30443 = HTTPS + ALTERNATIVE_LOCALHOST_30443;
    private static final String ALTERNATIVE_LOCALHOST_8443 = ALTERNATIVE_LOCALHOST + ":30443";
    private static final String HTTPS_ALTERNATIVE_LOCALHOST_8443 = HTTPS + ALTERNATIVE_LOCALHOST_8443;
    private static final String ALTERNATIVE_SPACE_LOCALHOST = "alternative localhost";
    private static final String LOCALHOST = "localhost";
    private static final String LOCALHOST_8443 = LOCALHOST + ":8443";
    private static final String HTTPS_LOCALHOST_8443 = HTTPS + LOCALHOST_8443;
    private static final String LOCALHOST_30443 = LOCALHOST + ":30443";
    private static final String HTTPS_LOCALHOST_30443 = HTTPS + LOCALHOST_30443;
    private static final String LOOPBACK_IP = "127.0.0.1";
    private static final String LOOPBACK_IP_8443 = LOOPBACK_IP + ":8443";
    private static final String HTTPS_LOOPBACK_IP_8443 = HTTPS + LOOPBACK_IP_8443;
    private static final String IP_A_OVERFLOW = "256.0.0.0";
    private static final String IP_B_OVERFLOW = "10.256.0.0";
    private static final String IP_C_OVERFLOW = "192.168.256.0";
    private static final String IP_D_OVERFLOW = "192.168.0.256";
    private static final String IP_TOO_MANY_OCTETS = "192.168.0.1.2";

    public static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(LOOPBACK_IP, LOOPBACK_IP,
                        HTTPS_LOCALHOST_8443, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(LOOPBACK_IP, LOCALHOST,
                        HTTPS_LOCALHOST_8443, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(ALTERNATIVE_LOCALHOST_8443, LOCALHOST_30443,
                        HTTPS_LOCALHOST_8443, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(ALTERNATIVE_LOCALHOST_8443, LOCALHOST_30443,
                        HTTPS_ALTERNATIVE_LOCALHOST_8443, HTTPS_LOCALHOST_30443))
                .add(Arguments.of(ALTERNATIVE_LOCALHOST_8443, LOCALHOST_30443,
                        HTTPS_ALTERNATIVE_LOCALHOST_30443, HTTPS_ALTERNATIVE_LOCALHOST_30443))
                .add(Arguments.of(ALTERNATIVE_LOCALHOST_8443, LOOPBACK_IP,
                        HTTPS_LOOPBACK_IP_8443, HTTPS_LOOPBACK_IP_8443))
                .add(Arguments.of(LOOPBACK_IP, ALTERNATIVE_LOCALHOST_8443,
                        HTTPS_LOOPBACK_IP_8443, HTTPS_LOOPBACK_IP_8443))
                .build();
    }

    public static Stream<Arguments> invalidParameterProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(LOOPBACK_IP, null))
                .add(Arguments.of(null, LOOPBACK_IP))
                .add(Arguments.of(IP_A_OVERFLOW, LOOPBACK_IP))
                .add(Arguments.of(LOCALHOST, IP_A_OVERFLOW))
                .add(Arguments.of(IP_B_OVERFLOW, LOOPBACK_IP))
                .add(Arguments.of(LOCALHOST, IP_B_OVERFLOW))
                .add(Arguments.of(IP_C_OVERFLOW, LOOPBACK_IP))
                .add(Arguments.of(LOCALHOST, IP_C_OVERFLOW))
                .add(Arguments.of(IP_D_OVERFLOW, LOOPBACK_IP))
                .add(Arguments.of(LOCALHOST, IP_D_OVERFLOW))
                .add(Arguments.of(IP_TOO_MANY_OCTETS, LOOPBACK_IP))
                .add(Arguments.of(LOCALHOST, IP_TOO_MANY_OCTETS))
                .add(Arguments.of(ALTERNATIVE_SPACE_LOCALHOST, LOOPBACK_IP))
                .add(Arguments.of(LOCALHOST, ALTERNATIVE_SPACE_LOCALHOST))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testVaultHostIsReplacedWithContainerHostWhenMatchesThePredefinedHost(
            final String fromHost, final String toHost, final URI vaultUri, final URI expectedUri) {
        //given
        final var underTest = new AuthorityOverrideFunction(fromHost, toHost);

        //when
        final var actual = underTest.apply(vaultUri);

        //then
        Assertions.assertEquals(expectedUri, actual);
    }

    @ParameterizedTest
    @MethodSource("invalidParameterProvider")
    void testConstructorShouldValidateInputWhenCalled(final String fromHost, final String toHost) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new AuthorityOverrideFunction(fromHost, toHost));

        //then + exception
    }
}
