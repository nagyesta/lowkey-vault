package com.github.nagyesta.lowkeyvault.filter;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;
import static org.mockito.Mockito.*;

class BaseUriExtractionFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    private AutoCloseable openMocks;

    public static Stream<Arguments> hostAndPortProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(LOCALHOST, HTTPS_PORT, EMPTY, HTTPS_LOCALHOST))
                .add(Arguments.of(LOCALHOST, HTTPS_PORT, KEYS, HTTPS_LOCALHOST))
                .add(Arguments.of(LOCALHOST, TOMCAT_SECURE_PORT, KEYS, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(LOCALHOST, TOMCAT_SECURE_PORT, VAULT_INVALID_VAULT_NAME_KEYS, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(LOCALHOST, HTTP_PORT, EMPTY, HTTPS_LOCALHOST_80))
                .add(Arguments.of(LOOP_BACK_IP, HTTPS_PORT, VAULT_INVALID_VAULT_NAME_KEYS, HTTPS_LOOP_BACK_IP))
                .add(Arguments.of(LOOP_BACK_IP, TOMCAT_SECURE_PORT, EMPTY, HTTPS_LOOP_BACK_IP_8443))
                .add(Arguments.of(LOOP_BACK_IP, HTTP_PORT, null, HTTPS_LOOP_BACK_IP_80))
                .add(Arguments.of(LOWKEY_VAULT, HTTPS_PORT, EMPTY, HTTPS_LOWKEY_VAULT))
                .add(Arguments.of(LOWKEY_VAULT, TOMCAT_SECURE_PORT, EMPTY, HTTPS_LOWKEY_VAULT_8443))
                .add(Arguments.of(LOWKEY_VAULT, HTTP_PORT, null, HTTPS_LOWKEY_VAULT_80))
                .add(Arguments.of(DEFAULT_LOWKEY_VAULT, HTTPS_PORT, EMPTY, HTTPS_DEFAULT_LOWKEY_VAULT))
                .add(Arguments.of(DEFAULT_LOWKEY_VAULT, TOMCAT_SECURE_PORT, EMPTY, HTTPS_DEFAULT_LOWKEY_VAULT_8443))
                .add(Arguments.of(DEFAULT_LOWKEY_VAULT, HTTP_PORT, KEYS, HTTPS_DEFAULT_LOWKEY_VAULT_80))
                .build();
    }

    public static Stream<Arguments> authResourceProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(LOCALHOST, HTTPS_LOCALHOST))
                .add(Arguments.of(AZURE_CLOUD, HTTPS_AZURE_CLOUD))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("hostAndPortProvider")
    void testDoFilterInternalShouldSetRequestBaseUriRequestAttributeWhenCalled(
            final String hostName, final int port, final String path, final URI expected) throws ServletException, IOException {
        //given
        final var underTest = new BaseUriExtractionFilter();
        when(request.getServerName()).thenReturn(hostName);
        when(request.getServerPort()).thenReturn(port);
        when(request.getRequestURI()).thenReturn(path);

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(request).getServerName();
        verify(request).getServerPort();
        verify(request).setAttribute(ApiConstants.REQUEST_BASE_URI, expected);
    }

    @Test
    void testShouldNotFilterShouldReturnTrueWhenRequestBaseUriIsPing() {
        //given
        final var underTest = new BaseUriExtractionFilter();
        when(request.getRequestURI()).thenReturn("/ping");

        //when
        final var actual = underTest.shouldNotFilter(request);

        //then
        Assertions.assertTrue(actual);
        verify(request, atLeastOnce()).getRequestURI();
    }
}
