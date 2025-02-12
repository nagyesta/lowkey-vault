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
        final ContentLengthHeaderRemover underTest = new ContentLengthHeaderRemover();
        final HttpRequest request = mock(HttpRequest.class);
        final HttpContext context = mock(HttpContext.class);

        //when
        underTest.process(request, context);

        //then
        verify(request).removeHeaders(eq(HTTP.CONTENT_LEN));
        verifyNoMoreInteractions(request, context);
    }
}
