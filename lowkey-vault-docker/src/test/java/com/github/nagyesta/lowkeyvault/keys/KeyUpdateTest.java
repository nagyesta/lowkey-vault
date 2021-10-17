package com.github.nagyesta.lowkeyvault.keys;

import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.KeyOperation;
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
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest(classes = ClientProviderConfig.class)
public class KeyUpdateTest extends BaseKeyTest {

    @Autowired
    public KeyUpdateTest(final ApacheHttpClientProvider provider) {
        super(provider);
    }

    public static Stream<Arguments> iterationProvider() {
        return KeyType.values().stream()
                .filter(k -> k != KeyType.OCT && k != KeyType.RSA && k != KeyType.EC)
                .flatMap(keyType -> IntStream.of(1, 2, 3)
                        .mapToObj(i -> Arguments.of(i, keyType)));
    }

    @ParameterizedTest
    @Timeout(20)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("update"), @Tag("version")})
    void testKeyVersionsShouldBeReturnedWhenTheyExistSync(final int count, final KeyType keyType) {
        //given
        final KeyClient keyClient = provider.getKeyClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> keyClient.createKey(name, keyType).getId());
        final KeyVaultKey key = keyClient.getKey(name);
        final KeyProperties newProperties = createNewProperties(key);

        //when
        final KeyVaultKey actual = keyClient.updateKeyProperties(newProperties, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY);

        //then
        assertMatches(key, actual);
    }

    @ParameterizedTest
    @Timeout(20)
    @MethodSource("iterationProvider")
    @Tags({@Tag("create"), @Tag("update"), @Tag("version")})
    void testKeyVersionsShouldBeReturnedWhenTheyExistAsync(final int count, final KeyType keyType) {
        //given
        final KeyAsyncClient keyClient = provider.getKeyAsyncClient();
        final String name = randomName();

        IntStream.range(0, count)
                .forEach(i -> Objects.requireNonNull(keyClient.createKey(name, keyType).block()).getId());
        final KeyVaultKey key = keyClient.getKey(name).block();
        final KeyProperties newProperties = createNewProperties(Objects.requireNonNull(key));

        //when
        final KeyVaultKey actual = keyClient.updateKeyProperties(newProperties, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY).block();

        //then
        assertMatches(key, actual);
    }

    private KeyProperties createNewProperties(final KeyVaultKey key) {
        final KeyProperties newProperties = key.getProperties();
        newProperties.setEnabled(false);
        newProperties.setExpiresOn(NOW);
        newProperties.setNotBefore(null);
        newProperties.setTags(Map.of("key1", "value1", "key2", "value2"));
        return newProperties;
    }

    private void assertMatches(@Nullable final KeyVaultKey key, @Nullable final KeyVaultKey actual) {
        Assertions.assertNotNull(key);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(key.getId(), actual.getId());
        Assertions.assertEquals(false, actual.getProperties().isEnabled());
        Assertions.assertEquals(2, actual.getKeyOperations().size());
        Assertions.assertTrue(actual.getKeyOperations().contains(KeyOperation.WRAP_KEY));
        Assertions.assertTrue(actual.getKeyOperations().contains(KeyOperation.UNWRAP_KEY));
        Assertions.assertEquals(NOW, actual.getProperties().getExpiresOn());
        Assertions.assertNull(actual.getProperties().getNotBefore());
        Assertions.assertEquals(2, actual.getProperties().getTags().size());
        Assertions.assertEquals("value1", actual.getProperties().getTags().get("key1"));
        Assertions.assertEquals("value2", actual.getProperties().getTags().get("key2"));
    }

}
