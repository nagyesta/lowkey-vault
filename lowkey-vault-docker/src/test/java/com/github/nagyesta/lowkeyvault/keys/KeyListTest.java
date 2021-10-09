package com.github.nagyesta.lowkeyvault.keys;

import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
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
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class KeyListTest extends BaseKeyTest {

    public KeyListTest() {
        super(new ApacheHttpClientProvider(ClientProviderConfig.HTTPS_LOCALHOST_8443 + "/vault/primary"));
    }

    public static Stream<Arguments> iterationProvider() {
        return Stream.of(KeyType.EC, KeyType.OCT_HSM)
                .flatMap(keyType -> IntStream.of(1, 2, 3, 42, 64)
                        .mapToObj(i -> Arguments.of(i, keyType)));
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("list")})
    void testKeysShouldBeReturnedWhenTheyExistSync(final int count, final KeyType keyType) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> {
                    final KeyVaultKey key = keyClient.createKey(name + "-" + i, keyType);
                    final String id = key.getId();
                    return id.substring(0, id.length() - 33);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        //when
        final PagedIterable<KeyProperties> actual = keyClient.listPropertiesOfKeys();

        //then
        final List<String> list = actual.stream()
                .map(KeyProperties::getId)
                .filter(s -> s.contains(name))
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("list")})
    void testKeysShouldBeReturnedWhenTheyExistAsync(final int count, final KeyType keyType) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .parallel()
                .mapToObj(i -> {
                    final KeyVaultKey key = keyClient.createKey(name + "-" + i, keyType).block();
                    final String id = Objects.requireNonNull(key).getId();
                    return id.substring(0, id.length() - 33);
                })
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());

        //when
        final List<KeyProperties> actual = keyClient.listPropertiesOfKeys().collectList().block();

        //then
        final List<String> list = Objects.requireNonNull(actual).stream()
                .map(KeyProperties::getId)
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
