package com.github.nagyesta.lowkeyvault.http;

import org.apache.http.HttpRequest;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class ContentLengthHeaderRemoverTest {

    @Test
    void testProcessShouldRemoveContentLengthWhenCalled() {
        //given
        final var underTest = new ContentLengthHeaderRemover();
        final var request = mock(HttpRequest.class);
        final var context = mock(HttpContext.class);

        //when
        underTest.process(request, context);

        //then
        verify(request).removeHeaders(eq(HTTP.CONTENT_LEN));
        verifyNoMoreInteractions(request, context);
    }
}
