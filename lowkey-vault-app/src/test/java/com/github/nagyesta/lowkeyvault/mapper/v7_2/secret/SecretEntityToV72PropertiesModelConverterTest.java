package com.github.nagyesta.lowkeyvault.mapper.v7_2.secret;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import lombok.Setter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.VERSIONED_SECRET_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.when;

class SecretEntityToV72PropertiesModelConverterTest {

    private SecretEntityToV72PropertiesModelConverter underTest;
    @Mock
    private VaultFake vault;
    @Mock
    private SecretVaultFake secretVault;
    @Mock
    private SecretConverterRegistry registry;

    private AutoCloseable openMocks;

    public static Stream<Arguments> validInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null, false))
                .add(Arguments.of(null, null, null, null, true))
                .add(Arguments.of(null, RecoveryLevel.PURGEABLE, null, null, true))
                .add(Arguments.of(INT_10, RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, null, null, true))
                .add(Arguments.of(INT_20, RecoveryLevel.RECOVERABLE_AND_PROTECTED_SUBSCRIPTION, null, null, false))
                .add(Arguments.of(INT_30, RecoveryLevel.CUSTOMIZED_RECOVERABLE_AND_PURGEABLE, TIME_10_MINUTES_AGO, null, true))
                .add(Arguments.of(INT_40, RecoveryLevel.RECOVERABLE, null, TIME_10_MINUTES_AGO, false))
                .add(Arguments.of(INT_50, RecoveryLevel.CUSTOMIZED_RECOVERABLE, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES, true))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        underTest = new SecretEntityToV72PropertiesModelConverter(registry);
        when(vault.secretVaultFake()).thenReturn(secretVault);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST);
        when(vault.matches(HTTPS_LOCALHOST, uri -> uri)).thenReturn(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new SecretEntityToV72PropertiesModelConverter(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testConvertShouldConvertAllFieldsWhenTheyAreSet(
            final Integer recoverableDays, final RecoveryLevel recoveryLevel,
            final OffsetDateTime notBefore, final OffsetDateTime expiry,
            final Boolean enabled) {

        //given
        final var input = new DummyKeyVaultSecretEntity();
        input.setEnabled(enabled);
        input.setExpiry(expiry);
        input.setNotBefore(notBefore);
        input.setRecoverableDays(recoverableDays);
        input.setRecoveryLevel(recoveryLevel);

        //when
        final var actual = underTest.convert(input, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertNotNull(actual.getCreatedOn());
        Assertions.assertNotNull(actual.getUpdatedOn());
        Assertions.assertEquals(enabled, actual.isEnabled());
        Assertions.assertEquals(input.getCreated(), actual.getCreatedOn());
        Assertions.assertEquals(expiry, actual.getExpiresOn());
        Assertions.assertEquals(notBefore, actual.getNotBefore());
        Assertions.assertEquals(recoverableDays, actual.getRecoverableDays());
        Assertions.assertEquals(recoveryLevel, actual.getRecoveryLevel());
        Assertions.assertEquals(input.getUpdated(), actual.getUpdatedOn());
    }

    @Setter
    private final class DummyKeyVaultSecretEntity extends KeyVaultSecretEntity {
        private Integer recoverableDays;
        private RecoveryLevel recoveryLevel;

        private DummyKeyVaultSecretEntity() {
            super(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, SecretEntityToV72PropertiesModelConverterTest.this.vault, LOCALHOST, null);
        }

        @Override
        public Integer getRecoverableDays() {
            return recoverableDays;
        }

        @Override
        public RecoveryLevel getRecoveryLevel() {
            return recoveryLevel;
        }

    }
}
