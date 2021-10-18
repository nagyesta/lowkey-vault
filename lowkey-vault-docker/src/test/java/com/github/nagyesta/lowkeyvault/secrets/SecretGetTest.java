package com.github.nagyesta.lowkeyvault.secrets;

import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.ClientProviderConfig;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class SecretGetTest extends BaseSecretTest {

    @Autowired
    public SecretGetTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> iterationProvider() {
        return IntStream.of(1, 2, 3, 42, 64)
                .mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("getSecret"), @Tag("secretVersion")})
    void testSecretVersionsShouldBeReturnedWhenTheyExistSync(final int count) {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .mapToObj(i -> secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).getId())
                .collect(Collectors.toList());

        //when
        final PagedIterable<SecretProperties> actual = secretClient.listPropertiesOfSecretVersions(name);

        //then
        final List<String> list = actual.stream()
                .map(SecretProperties::getId)
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("getSecret"), @Tag("secretVersion")})
    void testSecretVersionsShouldBeReturnedWhenTheyExistAsync(final int count) {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .mapToObj(i -> Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).block()).getId())
                .collect(Collectors.toList());

        //when
        final List<SecretProperties> actual = secretClient.listPropertiesOfSecretVersions(name).collectList().block();

        //then
        final List<String> list = Objects.requireNonNull(actual).stream()
                .map(SecretProperties::getId)
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
    }

    @Test
    @Tags({@Tag("createSecret"), @Tag("getSecret")})
    void testGetSecretShouldReturnTheRequestedVersionWhenItExistsAsync() {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = randomName();

        final KeyVaultSecret expected = Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).block());

        //when
        final KeyVaultSecret actual = secretClient.getSecret(name, expected.getProperties().getVersion()).block();

        //then
        assertSecretEquals(expected, actual);
    }

    @Test
    @Tags({@Tag("createSecret"), @Tag("getSecret")})
    void testGetSecretShouldReturnTheRequestedVersionWhenItExistsSync() {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = randomName();

        final KeyVaultSecret expected = Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)));

        //when
        final KeyVaultSecret actual = secretClient.getSecret(name, expected.getProperties().getVersion());

        //then
        assertSecretEquals(expected, actual);
    }

    @Test
    @Tags({@Tag("createSecret"), @Tag("getSecret")})
    void testGetSecretShouldReturnLatestVersionWhenMoreExistAsync() {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = randomName();

        Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).block()); //ignored
        final KeyVaultSecret expected = Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).block());

        //when
        final KeyVaultSecret actual = secretClient.getSecret(name).block();

        //then
        assertSecretEquals(expected, actual);
    }

    @Test
    @Tags({@Tag("createSecret"), @Tag("getSecret")})
    void testGetSecretShouldReturnLatestVersionWhenMoreExistSync() {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = randomName();

        Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT))); //ignored
        final KeyVaultSecret expected = Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)));

        //when
        final KeyVaultSecret actual = secretClient.getSecret(name);

        //then
        assertSecretEquals(expected, actual);
    }

    private void assertIdsEqual(final int count, final List<String> expected, final List<String> actual) {
        Assertions.assertEquals(count, actual.size());
        Assertions.assertIterableEquals(expected, actual);
    }

}
