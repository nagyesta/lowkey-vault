package com.github.nagyesta.lowkeyvault.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class ApacheHttpClientProviderTest {

    private static final String SPECIAL_LOCALHOST = "special.localhost";
    private static final String LOCALHOST = "localhost";
    private static final String HTTPS_SPECIAL_LOCALHOST_8443 = "https://" + SPECIAL_LOCALHOST + ":8443";
    private static final String HTTPS_LOCALHOST_8443 = "https://" + LOCALHOST + ":8443";
    private static final String WEB_KEY_ID = HTTPS_LOCALHOST_8443 + "/keys/test/00000000000000000000000000000001";

    @Test
    void testGetKeyAsyncClientShouldReturnClientWhenCalled() {
        //given
        final var underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, uri -> uri);

        //when
        final var client = underTest.getKeyAsyncClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetKeyClientShouldReturnClientWhenCalled() {
        //given
        final var underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, uri -> uri);

        //when
        final var client = underTest.getKeyClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetSecretAsyncClientShouldReturnClientWhenCalled() {
        //given
        final var underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, uri -> uri);

        //when
        final var client = underTest.getSecretAsyncClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetSecretClientShouldReturnClientWhenCalled() {
        //given
        final var underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, uri -> uri);

        //when
        final var client = underTest.getSecretClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetCryptoAsyncClientShouldReturnClientWhenCalled() {
        //given
        final var underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443);

        //when
        final var client = underTest.getCryptoAsyncClient(WEB_KEY_ID);

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetCryptoClientShouldReturnClientWhenCalled() {
        //given
        final var underTest = new ApacheHttpClientProvider(HTTPS_SPECIAL_LOCALHOST_8443,
                new AuthorityOverrideFunction(SPECIAL_LOCALHOST, LOCALHOST));

        //when
        final var client = underTest.getCryptoClient(WEB_KEY_ID);

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetLowkeyVaultManagementClientShouldReturnClientWhenCalled() {
        //given
        final var underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, uri -> uri);

        //when
        final var client = underTest.getLowkeyVaultManagementClient(mock(ObjectMapper.class));

        //then
        Assertions.assertNotNull(client);
    }
}
