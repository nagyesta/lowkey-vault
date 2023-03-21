package com.github.nagyesta.lowkeyvault.service.secret.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecretVaultFakeImplTest {

    public static Stream<Arguments> certificateCreationNullProvider() {
        final VersionedSecretEntityId entityId = VERSIONED_SECRET_ENTITY_ID_1_VERSION_1;
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null, null))
                .add(Arguments.of(entityId, null, null, null, null))
                .add(Arguments.of(null, LOWKEY_VAULT, null, null, null))
                .add(Arguments.of(null, LOWKEY_VAULT, CertContentType.PEM, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(entityId, null, CertContentType.PEM, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(entityId, LOWKEY_VAULT, null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(entityId, LOWKEY_VAULT, CertContentType.PEM, null, TIME_IN_10_MINUTES))
                .add(Arguments.of(entityId, LOWKEY_VAULT, CertContentType.PEM, TIME_10_MINUTES_AGO, null))
                .build();
    }

    public static Stream<Arguments> certificateCreationProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, LOWKEY_VAULT, CertContentType.PEM, TIME_10_MINUTES_AGO, NOW))
                .add(Arguments.of(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, LOOP_BACK_IP, CertContentType.PKCS12, NOW, TIME_IN_10_MINUTES))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecretVaultFakeImpl(null, null, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionShouldThrowExceptionWhenCalledWithNullName() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion((String) null, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionShouldThrowExceptionWhenCalledWithNullValue() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion(SECRET_NAME_1, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionUsingVersionedIdShouldThrowExceptionWhenCalledWithNullValue() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionUsingVersionedIdShouldThrowExceptionWhenCalledWithNullEntityId() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());
        final SecretCreateInput secretCreateInput = SecretCreateInput.builder()
                .value(LOWKEY_VAULT)
                .build();

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion((VersionedSecretEntityId) null, secretCreateInput));

        //then + exception
    }

    @Test
    void testCreateSecretVersionShouldCreateNewEntityWhenCalledWithValidInput() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());

        //when
        final VersionedSecretEntityId secretVersion = underTest.createSecretVersion(SECRET_NAME_1, SecretCreateInput.builder()
                .value(LOWKEY_VAULT)
                .build());

        //then
        final ReadOnlyKeyVaultSecretEntity actual = underTest.getEntities().getReadOnlyEntity(secretVersion);
        Assertions.assertNull(actual.getContentType());
        Assertions.assertEquals(LOWKEY_VAULT, actual.getValue());
        Assertions.assertEquals(secretVersion.asUri(HTTPS_LOCALHOST_8443), actual.getId().asUri(HTTPS_LOCALHOST_8443));
    }

    @Test
    void testCreateVersionedSecretEntityIdShouldCreateNewEntityIdWhenCalledWithValidInput() {
        //given
        final VaultFake vaultFake = mock(VaultFake.class);
        when(vaultFake.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest = new SecretVaultFakeImpl(vaultFake, RecoveryLevel.PURGEABLE, null);

        //when
        final VersionedSecretEntityId actual = underTest.createVersionedId(SECRET_NAME_1, SECRET_VERSION_1);

        //then
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, actual);
    }

    @SuppressWarnings("DataFlowIssue")
    @ParameterizedTest
    @MethodSource("certificateCreationNullProvider")
    void testCreateSecretVersionForCertificateShouldThrowExceptionWhenCalledWithNulls(
            final VersionedSecretEntityId id,
            final String value,
            final CertContentType contentType,
            final OffsetDateTime notBefore,
            final OffsetDateTime expiry) {
        //given
        final VaultFake vaultFake = mock(VaultFake.class);
        when(vaultFake.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest = new SecretVaultFakeImpl(vaultFake, RecoveryLevel.PURGEABLE, null);
        final SecretCreateInput secretCreateInput = Optional.ofNullable(value)
                .map(v -> SecretCreateInput.builder()
                        .value(v)
                        .contentType(Optional.ofNullable(contentType).map(CertContentType::getMimeType).orElse(null))
                        .notBefore(notBefore)
                        .expiresOn(expiry)
                        .managed(true)
                        .build())
                .orElse(null);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createSecretVersion(id, secretCreateInput));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("certificateCreationProvider")
    void testCreateSecretVersionForCertificateShouldSetValuesAndManagedFlagWhenCalledWithValidInput(
            final VersionedSecretEntityId id,
            final String value,
            final CertContentType contentType,
            final OffsetDateTime notBefore,
            final OffsetDateTime expiry) {
        //given
        final VaultFake vaultFake = mock(VaultFake.class);
        when(vaultFake.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest = new SecretVaultFakeImpl(vaultFake, RecoveryLevel.PURGEABLE, null);
        final SecretCreateInput secretCreateInput = SecretCreateInput.builder()
                .value(value)
                .contentType(contentType.getMimeType())
                .notBefore(notBefore)
                .createdOn(notBefore)
                .updatedOn(notBefore)
                .expiresOn(expiry)
                .managed(true)
                .enabled(true)
                .build();

        //when
        final VersionedSecretEntityId actual = underTest.createSecretVersion(id, secretCreateInput);

        //then
        final ReadOnlyKeyVaultSecretEntity entity = underTest.getEntities().getReadOnlyEntity(actual);
        Assertions.assertEquals(id.id(), entity.getId().id());
        Assertions.assertEquals(id.version(), entity.getId().version());
        Assertions.assertEquals(value, entity.getValue());
        Assertions.assertEquals(contentType.getMimeType(), entity.getContentType());
        Assertions.assertEquals(notBefore, entity.getNotBefore().orElse(null));
        Assertions.assertEquals(expiry, entity.getExpiry().orElse(null));
        Assertions.assertTrue(entity.isEnabled());
        Assertions.assertTrue(entity.isManaged());
        //created and updated must be set to the same value as not before in case of new certificate backing secrets
        Assertions.assertEquals(notBefore, entity.getCreated());
        Assertions.assertEquals(notBefore, entity.getUpdated());
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionShouldThrowExceptionWhenCalledWithUpdatedOnEarlierThanCreatedOn() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());
        final SecretCreateInput secretCreateInput = SecretCreateInput.builder()
                .value(LOWKEY_VAULT)
                .createdOn(TIME_IN_10_MINUTES)
                .updatedOn(TIME_10_MINUTES_AGO)
                .build();

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion(SECRET_NAME_1, secretCreateInput));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionShouldSetBothValuesWhenCalledWithCreatedOnAndNoUpdatedOn() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());
        final SecretCreateInput secretCreateInput = SecretCreateInput.builder()
                .value(LOWKEY_VAULT)
                .createdOn(TIME_10_MINUTES_AGO)
                .build();

        //when
        final VersionedSecretEntityId actual = underTest.createSecretVersion(SECRET_NAME_1, secretCreateInput);

        //then
        final ReadOnlyKeyVaultSecretEntity entity = underTest.getEntities().getReadOnlyEntity(actual);
        Assertions.assertEquals(TIME_10_MINUTES_AGO, entity.getCreated());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, entity.getUpdated());
    }

    @Test
    void testCreateSecretVersionShouldSetBothValuesWhenCalledWithUpdatedOnFromFutureAndNoCreatedOn() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());
        final SecretCreateInput secretCreateInput = SecretCreateInput.builder()
                .value(LOWKEY_VAULT)
                .updatedOn(TIME_IN_10_MINUTES)
                .build();

        //when
        final VersionedSecretEntityId actual = underTest.createSecretVersion(SECRET_NAME_1, secretCreateInput);

        //then
        final ReadOnlyKeyVaultSecretEntity entity = underTest.getEntities().getReadOnlyEntity(actual);
        Assertions.assertTrue(entity.getCreated().isAfter(NOW));
        Assertions.assertTrue(entity.getCreated().isBefore(TIME_IN_10_MINUTES));
        Assertions.assertEquals(TIME_IN_10_MINUTES, entity.getUpdated());
    }

    @Test
    void testCreateSecretVersionShouldThrowExceptionWhenCalledWithUpdatedOnFromThePastAndNoCreatedOn() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());
        final SecretCreateInput secretCreateInput = SecretCreateInput.builder()
                .value(LOWKEY_VAULT)
                .updatedOn(TIME_10_MINUTES_AGO)
                .build();

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion(SECRET_NAME_1, secretCreateInput));

        //then + exception
    }
}
