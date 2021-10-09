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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class KeyGetTest extends BaseKeyTest {

    @Autowired
    public KeyGetTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> iterationProvider() {
        return KeyType.values().stream()
                .filter(k -> k != KeyType.OCT && k != KeyType.RSA && k != KeyType.EC)
                .flatMap(keyType -> IntStream.of(1, 2, 3, 42, 64)
                        .mapToObj(i -> Arguments.of(i, keyType)));
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("get"), @Tag("version")})
    void testKeyVersionsShouldBeReturnedWhenTheyExistSync(final int count, final KeyType keyType) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .mapToObj(i -> keyClient.createKey(name, keyType).getId())
                .collect(Collectors.toList());

        //when
        final PagedIterable<KeyProperties> actual = keyClient.listPropertiesOfKeyVersions(name);

        //then
        final List<String> list = actual.stream()
                .map(KeyProperties::getId)
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
    }

    @ParameterizedTest
    @Timeout(30)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("get"), @Tag("version")})
    void testKeyVersionsShouldBeReturnedWhenTheyExistAsync(final int count, final KeyType keyType) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();

        final List<String> expected = IntStream.range(0, count)
                .mapToObj(i -> Objects.requireNonNull(keyClient.createKey(name, keyType).block()).getId())
                .collect(Collectors.toList());

        //when
        final List<KeyProperties> actual = keyClient.listPropertiesOfKeyVersions(name).collectList().block();

        //then
        final List<String> list = Objects.requireNonNull(actual).stream()
                .map(KeyProperties::getId)
                .collect(Collectors.toList());
        assertIdsEqual(count, expected, list);
    }

    @ParameterizedTest
    @MethodSource("keyTypeProvider")
    @Tags({@Tag("create"), @Tag("get")})
    void testGetKeyShouldReturnTheRequestedVersionWhenItExistsAsync(final KeyType keyType) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();

        final KeyVaultKey expected = Objects.requireNonNull(keyClient.createKey(name, keyType).block());

        //when
        final KeyVaultKey actual = keyClient.getKey(name, expected.getProperties().getVersion()).block();

        //then
        assertKeyEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("keyTypeProvider")
    @Tags({@Tag("create"), @Tag("get")})
    void testGetKeyShouldReturnTheRequestedVersionWhenItExistsSync(final KeyType keyType) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();

        final KeyVaultKey expected = Objects.requireNonNull(keyClient.createKey(name, keyType));

        //when
        final KeyVaultKey actual = keyClient.getKey(name, expected.getProperties().getVersion());

        //then
        assertKeyEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("keyTypeProvider")
    @Tags({@Tag("create"), @Tag("get")})
    void testGetKeyShouldReturnLatestVersionWhenMoreExistAsync(final KeyType keyType) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();

        Objects.requireNonNull(keyClient.createKey(name, keyType).block()); //ignored
        final KeyVaultKey expected = Objects.requireNonNull(keyClient.createKey(name, keyType).block());

        //when
        final KeyVaultKey actual = keyClient.getKey(name).block();

        //then
        assertKeyEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("keyTypeProvider")
    @Tags({@Tag("create"), @Tag("get")})
    void testGetKeyShouldReturnLatestVersionWhenMoreExistSync(final KeyType keyType) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();

        Objects.requireNonNull(keyClient.createKey(name, keyType)); //ignored
        final KeyVaultKey expected = Objects.requireNonNull(keyClient.createKey(name, keyType));

        //when
        final KeyVaultKey actual = keyClient.getKey(name);

        //then
        assertKeyEquals(expected, actual);
    }

    private void assertIdsEqual(final int count, final List<String> expected, final List<String> actual) {
        Assertions.assertEquals(count, actual.size());
        Assertions.assertIterableEquals(expected, actual);
    }

}
