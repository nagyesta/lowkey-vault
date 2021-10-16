package com.github.nagyesta.lowkeyvault.keys;

import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.DeletedKey;
import com.azure.security.keyvault.keys.models.KeyType;
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

import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class KeyDeleteTest extends BaseKeyTest {

    @Autowired
    public KeyDeleteTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> iterationProvider() {
        return KeyType.values().stream()
                .filter(k -> k != KeyType.OCT && k != KeyType.EC_HSM && k != KeyType.RSA_HSM)
                .flatMap(keyType -> IntStream.of(1, 2, 3)
                        .mapToObj(i -> Arguments.of(i, keyType)));
    }

    @ParameterizedTest
    @Timeout(10)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("delete")})
    void testKeyDeleteShouldReturnKeyDetailsWhenTheyExistSync(final int count, final KeyType keyType) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> keyClient.createKey(name, keyType).getId());

        //when
        final DeletedKey actual = keyClient.beginDeleteKey(name).waitForCompletion().getValue();

        //then
        Assertions.assertTrue(actual.getRecoveryId().contains(name));
        Assertions.assertTrue(NOW.isBefore(actual.getDeletedOn()));
        Assertions.assertTrue(NOW.plusDays(90).isBefore(actual.getScheduledPurgeDate()));
    }

    @ParameterizedTest
    @Timeout(10)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("delete")})
    void testKeyDeleteShouldReturnKeyDetailsWhenTheyExistAsync(final int count, final KeyType keyType) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> Objects.requireNonNull(keyClient.createKey(name, keyType).block()).getId());

        //when
        final DeletedKey actual = keyClient.beginDeleteKey(name).getSyncPoller().waitForCompletion().getValue();

        //then
        Assertions.assertTrue(actual.getRecoveryId().contains(name));
        Assertions.assertTrue(NOW.isBefore(actual.getDeletedOn()));
        Assertions.assertTrue(NOW.plusDays(90).isBefore(actual.getScheduledPurgeDate()));
    }

}
