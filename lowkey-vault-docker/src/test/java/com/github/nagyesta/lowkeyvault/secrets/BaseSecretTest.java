package com.github.nagyesta.lowkeyvault.secrets;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import org.junit.jupiter.api.Assertions;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

public class BaseSecretTest {

    protected static final String SYNC = "-sync";
    protected static final String ASYNC = "-async";
    protected static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS);
    protected static final OffsetDateTime TIME_IN_AN_HOUR = NOW.plusHours(1).truncatedTo(ChronoUnit.SECONDS);
    protected static final Map<String, String> TAG_MAP = Map.of("tag1", "value1", "tag2", "value2");

    protected ApacheHttpClientProvider provider;

    protected BaseSecretTest(final ApacheHttpClientProvider provider) {
        this.provider = provider;
    }

    protected String randomName() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    protected void assertSecretEquals(final KeyVaultSecret expected, final KeyVaultSecret actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getProperties());
        Assertions.assertNotNull(actual.getProperties().getVersion());
        Assertions.assertEquals(expected.getId(), actual.getId());
        Assertions.assertEquals(expected.getValue(), actual.getValue());
        Assertions.assertEquals(expected.getProperties().getContentType(), actual.getProperties().getContentType());
        Assertions.assertEquals(expected.getProperties().getRecoveryLevel(), actual.getProperties().getRecoveryLevel());
        Assertions.assertEquals(expected.getProperties().getRecoverableDays(), actual.getProperties().getRecoverableDays());
        Assertions.assertEquals(expected.getProperties().getCreatedOn(), actual.getProperties().getCreatedOn());
        Assertions.assertEquals(expected.getProperties().getUpdatedOn(), actual.getProperties().getUpdatedOn());
        Assertions.assertEquals(expected.getProperties().getExpiresOn(), actual.getProperties().getExpiresOn());
        Assertions.assertEquals(expected.getProperties().getNotBefore(), actual.getProperties().getNotBefore());
        Assertions.assertEquals(expected.getName(), actual.getName());
    }
}
