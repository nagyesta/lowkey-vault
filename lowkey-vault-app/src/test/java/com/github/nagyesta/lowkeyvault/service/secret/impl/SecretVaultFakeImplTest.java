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
                .add(Arguments.of(null, null, CertContentType.PEM, null, null))
                .add(Arguments.of(null, null, null, TIME_10_MINUTES_AGO, null))
                .add(Arguments.of(null, null, null, null, TIME_IN_10_MINUTES))
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
                () -> underTest.createSecretVersion((String) null, LOWKEY_VAULT, null));

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
                () -> underTest.createSecretVersion(SECRET_NAME_1, null, null));

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
                () -> underTest.createSecretVersion(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, null, null));

        //then + exception
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testCreateSecretVersionUsingVersionedIdShouldThrowExceptionWhenCalledWithNullEntityId() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersion((VersionedSecretEntityId) null, LOWKEY_VAULT, null));

        //then + exception
    }

    @Test
    void testCreateSecretVersionShouldCreateNewEntityWhenCalledWithValidInput() {
        //given
        final VaultFake vaultFake = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final SecretVaultFakeImpl underTest =
                new SecretVaultFakeImpl(vaultFake, vaultFake.getRecoveryLevel(), vaultFake.getRecoverableDays());

        //when
        final VersionedSecretEntityId secretVersion = underTest.createSecretVersion(SECRET_NAME_1, LOWKEY_VAULT, null);

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

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> underTest.createSecretVersionForCertificate(id, value, contentType, notBefore, expiry));

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

        //when
        final VersionedSecretEntityId actual = underTest
                .createSecretVersionForCertificate(id, value, contentType, notBefore, expiry);

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
}
