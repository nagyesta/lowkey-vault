package com.github.nagyesta.lowkeyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.ClientProviderConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.lang.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class SecretUpdateTest extends BaseSecretTest {

    @Autowired
    public SecretUpdateTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> iterationProvider() {
        return IntStream.of(1, 2, 3)
                .mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @Timeout(20)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("updateSecret"), @Tag("secretVersion")})
    void testSecretVersionsShouldBeReturnedWhenTheyExistSync(final int count) {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).getId());
        final KeyVaultSecret secret = secretClient.getSecret(name);
        final SecretProperties newProperties = createNewProperties(secret);

        //when
        final SecretProperties actual = secretClient.updateSecretProperties(newProperties);

        //then
        assertMatches(secret, actual);
    }

    @ParameterizedTest
    @Timeout(20)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("updateSecret"), @Tag("secretVersion")})
    void testSecretVersionsShouldBeReturnedWhenTheyExistAsync(final int count) {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).block()).getId());
        final KeyVaultSecret secret = secretClient.getSecret(name).block();
        final SecretProperties newProperties = createNewProperties(Objects.requireNonNull(secret));

        //when
        final SecretProperties actual = secretClient.updateSecretProperties(newProperties).block();

        //then
        assertMatches(secret, actual);
    }

    private SecretProperties createNewProperties(final KeyVaultSecret secret) {
        final SecretProperties newProperties = secret.getProperties();
        newProperties.setEnabled(false);
        newProperties.setExpiresOn(NOW);
        newProperties.setNotBefore(null);
        newProperties.setTags(Map.of("key1", "value1", "key2", "value2"));
        return newProperties;
    }

    private void assertMatches(@Nullable final KeyVaultSecret secret, @Nullable final SecretProperties actual) {
        Assertions.assertNotNull(secret);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(secret.getId(), actual.getId());
        Assertions.assertEquals(false, actual.isEnabled());
        Assertions.assertEquals(NOW, actual.getExpiresOn());
        Assertions.assertNull(actual.getNotBefore());
        Assertions.assertEquals(2, actual.getTags().size());
        Assertions.assertEquals("value1", actual.getTags().get("key1"));
        Assertions.assertEquals("value2", actual.getTags().get("key2"));
    }

}
