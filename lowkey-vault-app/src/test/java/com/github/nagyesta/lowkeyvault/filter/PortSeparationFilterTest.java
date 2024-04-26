package com.github.nagyesta.lowkeyvault.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class PortSeparationFilterTest {

    @Test
    void testDoFilterInternalShouldCallChainWhenInsecureAndTokenRequest() throws ServletException, IOException {
        //given
        final PortSeparationFilter underTest = new PortSeparationFilter();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        when(request.isSecure()).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/metadata/identity/oauth2/token");

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(chain).doFilter(same(request), same(response));
    }

    @Test
    void testDoFilterInternalShouldCallChainWhenSecureAndNotTokenRequest() throws ServletException, IOException {
        //given
        final PortSeparationFilter underTest = new PortSeparationFilter();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        when(request.isSecure()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/ping");

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(chain).doFilter(same(request), same(response));
    }

    @Test
    void testDoFilterInternalShouldReturnNotFoundWhenSecureAndTokenRequest() throws ServletException, IOException {
        //given
        final PortSeparationFilter underTest = new PortSeparationFilter();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        when(request.isSecure()).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/metadata/identity/oauth2/token");

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void testDoFilterInternalShouldReturnNotFoundWhenInsecureAndNotTokenRequest() throws ServletException, IOException {
        //given
        final PortSeparationFilter underTest = new PortSeparationFilter();
        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);
        final FilterChain chain = mock(FilterChain.class);
        when(request.isSecure()).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/ping");

        //when
        underTest.doFilterInternal(request, response, chain);

        //then
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND);
    }
}
