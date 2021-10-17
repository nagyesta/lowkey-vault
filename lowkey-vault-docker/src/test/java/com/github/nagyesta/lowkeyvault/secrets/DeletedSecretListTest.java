package com.github.nagyesta.lowkeyvault.secrets;

import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
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
public class DeletedSecretListTest extends BaseSecretTest {

    public DeletedSecretListTest() {
        super(new ApacheHttpClientProvider(ClientProviderConfig.HTTPS_LOCALHOST_8443 + "/vault/primary"));
    }

    public static Stream<Arguments> iterationProvider() {
        return IntStream.of(1, 2, 3, 5, 51)
                .mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @Timeout(60)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("deleteSecret"), @Tag("listDeletedSecret")})
    void testSecretsShouldBeReturnedWhenTheyExistSync(final int count) {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> {
                    final KeyVaultSecret secret = secretClient.setSecret(name + "-" + i, name.toUpperCase(Locale.ROOT));
                    final String id = secret.getId().replace("/secrets/", "/deletedsecrets/");
                    secretClient.beginDeleteSecret(name + "-" + i).waitForCompletion();
                    return id.substring(0, id.length() - 33);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        //when
        final PagedIterable<DeletedSecret> actual = secretClient.listDeletedSecrets();

        //then
        final List<String> list = actual.stream()
                .map(DeletedSecret::getRecoveryId)
                .filter(s -> s.contains(name))
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
        assertDeleteInfoPresent(name, actual.stream().collect(Collectors.toList()));
    }

    @ParameterizedTest
    @Timeout(60)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("deleteSecret"), @Tag("listDeletedSecret")})
    void testSecretsShouldBeReturnedWhenTheyExistAsync(final int count) {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> {
                    final KeyVaultSecret secret = secretClient.setSecret(name + "-" + i, name.toUpperCase(Locale.ROOT)).block();
                    final String id = Objects.requireNonNull(secret).getId().replace("/secrets/", "/deletedsecrets/");
                    secretClient.beginDeleteSecret(name + "-" + i).getSyncPoller().waitForCompletion();
                    return id.substring(0, id.length() - 33);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        //when
        final List<DeletedSecret> actual = secretClient.listDeletedSecrets().collectList().block();

        //then
        final List<String> list = Objects.requireNonNull(actual).stream()
                .map(DeletedSecret::getRecoveryId)
                .filter(s -> s.contains(name))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
        assertDeleteInfoPresent(name, actual);
    }

    private void assertDeleteInfoPresent(final String name, final List<DeletedSecret> actual) {
        actual.stream()
                .filter(deletedSecret -> deletedSecret.getRecoveryId().contains(name))
                .forEach(deletedSecret -> {
                    Assertions.assertTrue(NOW.isBefore(deletedSecret.getDeletedOn()));
                    Assertions.assertTrue(NOW.plusDays(90).isBefore(deletedSecret.getScheduledPurgeDate()));
                });
    }

    private void assertIdsEqual(final int count, final List<String> expected, final List<String> actual) {
        Assertions.assertEquals(count, actual.size());
        Assertions.assertIterableEquals(expected, actual);
    }

}
