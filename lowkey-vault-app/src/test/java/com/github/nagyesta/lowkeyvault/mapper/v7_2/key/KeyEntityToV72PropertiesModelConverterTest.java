package com.github.nagyesta.lowkeyvault.mapper.v7_2.key;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.SignatureAlgorithm;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOWKEY_VAULT;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class KeyEntityToV72PropertiesModelConverterTest {

    @InjectMocks
    private KeyEntityToV72PropertiesModelConverter underTest;
    @Mock
    private VaultFake vault;
    @Mock
    private KeyVaultFake keyVault;

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
        when(vault.keyVaultFake()).thenReturn(keyVault);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST);
        when(vault.matches(eq(HTTPS_LOCALHOST))).thenReturn(true);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("validInputProvider")
    void testConvertShouldConvertAllFieldsWhenTheyAreSet(
            final Integer recoverableDays, final RecoveryLevel recoveryLevel,
            final OffsetDateTime notBefore, final OffsetDateTime expiry,
            final Boolean enabled) {

        //given
        final DummyKeyVaultKeyEntity input = new DummyKeyVaultKeyEntity();
        input.setEnabled(enabled);
        input.setExpiry(expiry);
        input.setNotBefore(notBefore);
        input.setRecoverableDays(recoverableDays);
        input.setRecoveryLevel(recoveryLevel);

        //when
        final KeyPropertiesModel actual = underTest.convert(input, HTTPS_LOWKEY_VAULT);

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

    private final class DummyKeyVaultKeyEntity extends KeyVaultKeyEntity<Integer, Integer> {
        private Integer recoverableDays;
        private RecoveryLevel recoveryLevel;

        private DummyKeyVaultKeyEntity() {
            super(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, KeyEntityToV72PropertiesModelConverterTest.this.vault, 1, 1, false);
        }

        @Override
        public KeyType getKeyType() {
            return null;
        }

        @Override
        public KeyCreationInput<?> keyCreationInput() {
            return null;
        }

        @Override
        public byte[] encryptBytes(final byte[] clear, final EncryptionAlgorithm encryptionAlgorithm,
                                   final byte[] iv) {
            return new byte[0];
        }

        @Override
        public byte[] decryptToBytes(final byte[] encrypted, final EncryptionAlgorithm encryptionAlgorithm,
                                     final byte[] iv) {
            return new byte[0];
        }

        @Override
        public Integer getRecoverableDays() {
            return recoverableDays;
        }

        public void setRecoverableDays(final Integer recoverableDays) {
            this.recoverableDays = recoverableDays;
        }

        @Override
        public RecoveryLevel getRecoveryLevel() {
            return recoveryLevel;
        }

        public void setRecoveryLevel(final RecoveryLevel recoveryLevel) {
            this.recoveryLevel = recoveryLevel;
        }

        @Override
        public byte[] signBytes(final byte[] clear, final SignatureAlgorithm encryptionAlgorithm) {
            return new byte[0];
        }

        @Override
        public boolean verifySignedBytes(final byte[] signed,
                                         final SignatureAlgorithm encryptionAlgorithm,
                                         final byte[] digest) {
            return false;
        }
    }
}
