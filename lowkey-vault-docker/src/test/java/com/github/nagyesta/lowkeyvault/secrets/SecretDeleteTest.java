package com.github.nagyesta.lowkeyvault.secrets;

import com.azure.security.keyvault.secrets.SecretAsyncClient;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.DeletedSecret;
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

import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class SecretDeleteTest extends BaseSecretTest {

    @Autowired
    public SecretDeleteTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> iterationProvider() {
        return IntStream.of(1, 2, 3, 42)
                .mapToObj(Arguments::of);
    }

    @ParameterizedTest
    @Timeout(10)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("deleteSecret")})
    void testSecretDeleteShouldReturnSecretDetailsWhenTheyExistSync(final int count) {
        //given
        final SecretClient secretClient = provider.getSecretClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).getId());

        //when
        final DeletedSecret actual = secretClient.beginDeleteSecret(name).waitForCompletion().getValue();

        //then
        Assertions.assertTrue(actual.getRecoveryId().contains(name));
        Assertions.assertTrue(NOW.isBefore(actual.getDeletedOn()));
        Assertions.assertTrue(NOW.plusDays(90).isBefore(actual.getScheduledPurgeDate()));
    }

    @ParameterizedTest
    @Timeout(10)
    @MethodSource("iterationProvider")
    @Tags({@Tag("createSecret"), @Tag("deleteSecret")})
    void testSecretDeleteShouldReturnSecretDetailsWhenTheyExistAsync(final int count) {
        //given
        final SecretAsyncClient secretClient = provider.getSecretAsyncClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> Objects.requireNonNull(secretClient.setSecret(name, name.toUpperCase(Locale.ROOT)).block()).getId());

        //when
        final DeletedSecret actual = secretClient.beginDeleteSecret(name).getSyncPoller().waitForCompletion().getValue();

        //then
        Assertions.assertTrue(actual.getRecoveryId().contains(name));
        Assertions.assertTrue(NOW.isBefore(actual.getDeletedOn()));
        Assertions.assertTrue(NOW.plusDays(90).isBefore(actual.getScheduledPurgeDate()));
    }

}
