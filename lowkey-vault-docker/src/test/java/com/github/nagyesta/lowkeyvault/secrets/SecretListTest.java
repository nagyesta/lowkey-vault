package com.github.nagyesta.lowkeyvault.secrets;

import com.azure.core.http.rest.PagedIterable;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class SecretListTest extends BaseSecretTest {

    public SecretListTest() {
        super(new ApacheHttpClientProvider(ClientProviderConfig.HTTPS_LOCALHOST_8443 + "/vault/primary"));
    }

    public static Stream<Arguments> iterationProvider() {
        return IntStream.of(1, 2, 3, 42, 64)
                .mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("listSecret")})
    void testSecretsShouldBeReturnedWhenTheyExistSync(final int count) {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> {
                    final KeyVaultSecret secret = secretClient.setSecret(name + "-" + i, name.toUpperCase(Locale.ROOT));
                    final String id = secret.getId();
                    return id.substring(0, id.length() - 33);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        //when
        final PagedIterable<SecretProperties> actual = secretClient.listPropertiesOfSecrets();

        //then
        final List<String> list = actual.stream()
                .map(SecretProperties::getId)
                .filter(s -> s.contains(name))
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("listSecret")})
    void testSecretsShouldBeReturnedWhenTheyExistAsync(final int count) {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> {
                    final KeyVaultSecret secret = secretClient.setSecret(name + "-" + i, name.toUpperCase(Locale.ROOT)).block();
                    final String id = Objects.requireNonNull(secret).getId();
                    return id.substring(0, id.length() - 33);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        //when
        final List<SecretProperties> actual = secretClient.listPropertiesOfSecrets().collectList().block();

        //then
        final List<String> list = Objects.requireNonNull(actual).stream()
                .map(SecretProperties::getId)
                .filter(s -> s.contains(name))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
    }

    private void assertIdsEqual(final int count, final List<String> expected, final List<String> actual) {
        Assertions.assertEquals(count, actual.size());
        Assertions.assertIterableEquals(expected, actual);
    }

}
