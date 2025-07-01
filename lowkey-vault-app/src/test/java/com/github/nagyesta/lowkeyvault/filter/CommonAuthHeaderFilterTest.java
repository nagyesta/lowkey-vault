package com.github.nagyesta.lowkeyvault.filter;

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
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_AZURE_CLOUD;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static org.mockito.Mockito.*;

class CommonAuthHeaderFilterTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private FilterChain chain;

    private AutoCloseable openMocks;

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
    @NullSource
    @ValueSource(strings = {EMPTY, HEADER_VALUE})
    void testDoFilterInternalShouldNotCallNextOnChainWhenAuthorizationHeaderMissing(final String headerValue)
            throws ServletException, IOException {
        //given
        final var underTest = new CommonAuthHeaderFilter(CommonAuthHeaderFilter.OMIT_DEFAULT);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(headerValue);

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(request, atLeastOnce()).getHeader(HttpHeaders.AUTHORIZATION);
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
        final var underTest = new CommonAuthHeaderFilter(CommonAuthHeaderFilter.OMIT_DEFAULT);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(headerValue);

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(response).setHeader(eq(HttpHeaders.WWW_AUTHENTICATE), anyString());
    }

    @ParameterizedTest
    @MethodSource("authResourceProvider")
    void testDoFilterInternalShouldSetResourceOnResponseHeaderWhenCalled(final String authResource, final URI expected)
            throws ServletException, IOException {
        //given
        final var underTest = new CommonAuthHeaderFilter(authResource);
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(HEADER_VALUE);

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(response).setHeader(eq(HttpHeaders.WWW_AUTHENTICATE), contains("resource=\"" + expected + "\""));
    }

    @Test
    void testShouldNotFilterShouldReturnTrueWhenRequestBaseUriIsPing() {
        //given
        final var underTest = new CommonAuthHeaderFilter(CommonAuthHeaderFilter.OMIT_DEFAULT);
        when(request.getRequestURI()).thenReturn("/ping");

        //when
        final var actual = underTest.shouldNotFilter(request);

        //then
        Assertions.assertTrue(actual);
        verify(request, atLeastOnce()).getRequestURI();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new CommonAuthHeaderFilter(null));

        //then + exception
    }
}
