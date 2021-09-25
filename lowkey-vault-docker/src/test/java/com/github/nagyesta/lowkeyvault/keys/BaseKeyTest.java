package com.github.nagyesta.lowkeyvault.keys;

import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class BaseKeyTest {

    protected static final String SYNC = "-sync";
    protected static final String ASYNC = "-async";
    protected static final KeyOperation OPERATION = KeyOperation.UNWRAP_KEY;
    protected static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    protected static final OffsetDateTime IN_AN_HOUR = NOW.plusHours(1).truncatedTo(ChronoUnit.SECONDS);
    protected static final Map<String, String> TAG_MAP = Map.of("tag1", "value1", "tag2", "value2");

    protected ApacheHttpClientProvider provider;

    protected BaseKeyTest(final ApacheHttpClientProvider provider) {
        this.provider = provider;
    }

    public static Stream<Arguments> keyTypeProvider() {
        return KeyType.values().stream()
                .map(Arguments::of);
    }

    protected String randomName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    protected void assertKeyEquals(final KeyVaultKey expected, final KeyVaultKey actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getProperties());
        Assertions.assertNotNull(actual.getProperties().getVersion());
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getKey(), actual.getKey());
        Assertions.assertEquals(expected.getKeyType(), actual.getKeyType());
        Assertions.assertIterableEquals(expected.getKeyOperations(), actual.getKeyOperations());
        Assertions.assertEquals(expected.getProperties().getRecoveryLevel(), actual.getProperties().getRecoveryLevel());
        Assertions.assertEquals(expected.getProperties().getRecoverableDays(), actual.getProperties().getRecoverableDays());
        Assertions.assertEquals(expected.getProperties().getCreatedOn(), actual.getProperties().getCreatedOn());
        Assertions.assertEquals(expected.getProperties().getUpdatedOn(), actual.getProperties().getUpdatedOn());
        Assertions.assertEquals(expected.getProperties().getExpiresOn(), actual.getProperties().getExpiresOn());
        Assertions.assertEquals(expected.getProperties().getNotBefore(), actual.getProperties().getNotBefore());
        Assertions.assertEquals(expected.getName(), actual.getName());
    }
}
