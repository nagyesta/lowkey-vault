package com.github.nagyesta.lowkeyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.ClientProviderConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.TreeMap;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class SecretCreateTest extends BaseSecretTest {

    @Autowired
    public SecretCreateTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> validSecretProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("secret-1", "{\"value\":true}", "application/jsom"))
                .add(Arguments.of("secretTwo", "the quick brown fox", "text/plain"))
                .add(Arguments.of("secret-03", "jumps over", null))
                .add(Arguments.of("secret4", "<?xml version='1.0'?><none/>", "application/xml"))
                .build();
    }

    @ParameterizedTest
    @MethodSource("validSecretProvider")
    @Tags({@Tag("secret"), @Tag("createSecret")})
    void testNewSecretShouldBeCreatedWhenItDoesNotExistYetSync(final String secretName, final String value, final String contentType) {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = secretName + SYNC;

        //when
        final KeyVaultSecret secret = new KeyVaultSecret(name, value);
        secret.getProperties().setContentType(contentType)
                .setNotBefore(NOW)
                .setExpiresOn(TIME_IN_AN_HOUR)
                .setTags(TAG_MAP);
        final KeyVaultSecret actual = secretClient.setSecret(secret);

        //then
        assertSecret(name, value, contentType, actual);
    }

    @ParameterizedTest
    @MethodSource("validSecretProvider")
    @Tags({@Tag("secret"), @Tag("createSecret")})
    void testNewSecretShouldBeCreatedWhenItDoesNotExistYetAsync(final String secretName, final String value, final String contentType) {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = secretName + ASYNC;

        //when
        final KeyVaultSecret secret = new KeyVaultSecret(name, value);
        secret.getProperties().setContentType(contentType)
                .setNotBefore(NOW)
                .setExpiresOn(TIME_IN_AN_HOUR)
                .setTags(TAG_MAP);
        final KeyVaultSecret actual = secretClient.setSecret(secret).block();

        //then
        assertSecret(name, value, contentType, actual);
    }

    private void assertSecret(final String secretName, final String value, final String contentType, final KeyVaultSecret actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(secretName, actual.getName());
        Assertions.assertTrue(actual.getId().startsWith(provider.getVaultUrl()));
        Assertions.assertTrue(actual.getId().contains(secretName));
        Assertions.assertEquals(value, actual.getValue());
        Assertions.assertEquals(contentType, actual.getProperties().getContentType());
        Assertions.assertIterableEquals(new TreeMap<>(TAG_MAP).entrySet(), new TreeMap<>(actual.getProperties().getTags()).entrySet());
        Assertions.assertEquals(NOW, actual.getProperties().getNotBefore());
        Assertions.assertEquals(TIME_IN_AN_HOUR, actual.getProperties().getExpiresOn());
        Assertions.assertTrue(NOW.isBefore(actual.getProperties().getCreatedOn()));
        Assertions.assertTrue(NOW.isBefore(actual.getProperties().getUpdatedOn()));
    }
}
