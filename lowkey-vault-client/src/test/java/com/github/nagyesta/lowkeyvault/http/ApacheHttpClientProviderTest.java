package com.github.nagyesta.lowkeyvault.http;

import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApacheHttpClientProviderTest {

    private static final String HTTPS_LOCALHOST_8443 = "https://localhost:8443";
    private static final String WEB_KEY_ID = HTTPS_LOCALHOST_8443 + "/keys/test/00000000000000000000000000000001";
    @Test
    void testGetKeyAsyncClientShouldReturnClientWhenCalled() {
        //given
        final ApacheHttpClientProvider underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, false);

        //when
        final KeyAsyncClient client = underTest.getKeyAsyncClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetKeyClientShouldReturnClientWhenCalled() {
        //given
        final ApacheHttpClientProvider underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, false);

        //when
        final KeyClient client = underTest.getKeyClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetSecretAsyncClientShouldReturnClientWhenCalled() {
        //given
        final ApacheHttpClientProvider underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, false);

        //when
        final SecretAsyncClient client = underTest.getSecretAsyncClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetSecretClientShouldReturnClientWhenCalled() {
        //given
        final ApacheHttpClientProvider underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, false);

        //when
        final SecretClient client = underTest.getSecretClient();

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetCryptoAsyncClientShouldReturnClientWhenCalled() {
        //given
        final ApacheHttpClientProvider underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443);

        //when
        final CryptographyAsyncClient client = underTest.getCryptoAsyncClient(WEB_KEY_ID);

        //then
        Assertions.assertNotNull(client);
    }

    @Test
    void testGetCryptoClientShouldReturnClientWhenCalled() {
        //given
        final ApacheHttpClientProvider underTest = new ApacheHttpClientProvider(HTTPS_LOCALHOST_8443, true);

        //when
        final CryptographyClient client = underTest.getCryptoClient(WEB_KEY_ID);

        //then
        Assertions.assertNotNull(client);
    }
}
