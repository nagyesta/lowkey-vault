package com.github.nagyesta.lowkeyvault.filter;

import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CommonAuthHeaderFilterTest {

    private final CommonAuthHeaderFilter underTest = new CommonAuthHeaderFilter();
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    private AutoCloseable openMocks;

    public static Stream<Arguments> hostAndPortProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(LOCALHOST, HTTPS_PORT, HTTPS_LOCALHOST))
                .add(Arguments.of(LOCALHOST, TOMCAT_SECURE_PORT, HTTPS_LOCALHOST_8443))
                .add(Arguments.of(LOCALHOST, HTTP_PORT, HTTPS_LOCALHOST_80))
                .add(Arguments.of(LOOP_BACK_IP, HTTPS_PORT, HTTPS_LOOP_BACK_IP))
                .add(Arguments.of(LOOP_BACK_IP, TOMCAT_SECURE_PORT, HTTPS_LOOP_BACK_IP_8443))
                .add(Arguments.of(LOOP_BACK_IP, HTTP_PORT, HTTPS_LOOP_BACK_IP_80))
                .add(Arguments.of(LOWKEY_VAULT, HTTPS_PORT, HTTPS_LOWKEY_VAULT))
                .add(Arguments.of(LOWKEY_VAULT, TOMCAT_SECURE_PORT, HTTPS_LOWKEY_VAULT_8443))
                .add(Arguments.of(LOWKEY_VAULT, HTTP_PORT, HTTPS_LOWKEY_VAULT_80))
                .add(Arguments.of(DEFAULT_LOWKEY_VAULT, HTTPS_PORT, HTTPS_DEFAULT_LOWKEY_VAULT))
                .add(Arguments.of(DEFAULT_LOWKEY_VAULT, TOMCAT_SECURE_PORT, HTTPS_DEFAULT_LOWKEY_VAULT_8443))
                .add(Arguments.of(DEFAULT_LOWKEY_VAULT, HTTP_PORT, HTTPS_DEFAULT_LOWKEY_VAULT_80))
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
    @NullSource
    @ValueSource(strings = {EMPTY, HEADER_VALUE})
    void testDoFilterInternalShouldNotCallNextOnChainWhenAuthorizationHeaderMissing(final String headerValue)
            throws ServletException, IOException {
        //given
        when(request.getHeader(eq(HttpHeaders.AUTHORIZATION))).thenReturn(headerValue);

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(request, atLeastOnce()).getHeader(eq(HttpHeaders.AUTHORIZATION));
        if (StringUtils.hasText(headerValue)) {
            verify(chain).doFilter(same(request), same(response));
        } else {
            verifyNoInteractions(chain);
        }
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {EMPTY, HEADER_VALUE})
    void testDoFilterInternalShouldAddTokenToResponseHeaderWhenCalled(final String headerValue)
            throws ServletException, IOException {
        //given
        when(request.getHeader(eq(HttpHeaders.AUTHORIZATION))).thenReturn(headerValue);

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(response).setHeader(eq(HttpHeaders.WWW_AUTHENTICATE), anyString());
    }

    @ParameterizedTest
    @MethodSource("hostAndPortProvider")
    void testDoFilterInternalShouldSetRequestBaseUriRequestAttributeWhenCalled(
            final String hostName, final int port, final URI expected) throws ServletException, IOException {
        //given
        when(request.getServerName()).thenReturn(hostName);
        when(request.getServerPort()).thenReturn(port);

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(request).getServerName();
        verify(request).getServerPort();
        verify(request).setAttribute(eq(ApiConstants.REQUEST_BASE_URI), eq(expected));
    }
}
