package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.CERT_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.UNKNOWN;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class KeyVaultCertificateEntityTest {

    public static final int VALIDITY_MONTHS = 12;

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(CERT_NAME_1, null, null))
                .add(Arguments.of(null, CertificateCreationInput.builder().build(), null))
                .add(Arguments.of(null, null, mock(VaultFake.class)))
                .add(Arguments.of(null, CertificateCreationInput.builder().build(), mock(VaultFake.class)))
                .add(Arguments.of(CERT_NAME_1, null, mock(VaultFake.class)))
                .add(Arguments.of(CERT_NAME_1, CertificateCreationInput.builder().build(), null))
                .build();
    }

    public static Stream<Arguments> nullRenewalProvider() {
        final VersionedKeyEntityId kid = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        final VersionedCertificateEntityId cid = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final VaultFake vaultFake = mock(VaultFake.class);
        final CertificateCreationInput input = CertificateCreationInput.builder().build();
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(input, null, null, null))
                .add(Arguments.of(null, kid, null, null))
                .add(Arguments.of(null, null, cid, null))
                .add(Arguments.of(null, null, null, vaultFake))
                .add(Arguments.of(null, kid, cid, vaultFake))
                .add(Arguments.of(input, null, cid, vaultFake))
                .add(Arguments.of(input, kid, null, vaultFake))
                .add(Arguments.of(input, kid, cid, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final String id, final CertificateCreationInput input, final VaultFake vault) {
        //given


        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateEntity(id, input, vault));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("nullRenewalProvider")
    void testRenewalConstructorShouldThrowExceptionWhenCalledWithNulls(
            final ReadOnlyCertificatePolicy input, final VersionedKeyEntityId kid,
            final VersionedCertificateEntityId id, final VaultFake vault) {
        //given


        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateEntity(input, kid, id, vault));

        //then + exception
    }

    @SuppressWarnings("unchecked")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithAlreadyUsedKeyName() {
        //given
        final VersionedCertificateEntityId id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final CertificateCreationInput input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsName(eq(id.id()))).thenReturn(true);

        final KeyVaultFake keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(id.vault());
        when(vault.getRecoveryLevel()).thenReturn(RecoveryLevel.PURGEABLE);
        when(vault.getRecoverableDays()).thenReturn(null);
        when(vault.keyVaultFake()).thenReturn(keyFake);

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> new KeyVaultCertificateEntity(id.id(), input, vault));

        //then + exception
        verify(vault).keyVaultFake();
        verify(keyFake).getEntities();
        verify(keyMap).containsName(eq(id.id()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithAlreadyUsedSecretName() {
        //given
        final VersionedCertificateEntityId id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final CertificateCreationInput input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> secretMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(secretMap.containsName(eq(id.id()))).thenReturn(true);

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);

        final SecretVaultFake secretFake = mock(SecretVaultFake.class);
        when(secretFake.getEntities()).thenReturn(secretMap);
        final KeyVaultFake keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(id.vault());
        when(vault.getRecoveryLevel()).thenReturn(RecoveryLevel.PURGEABLE);
        when(vault.getRecoverableDays()).thenReturn(null);
        when(vault.secretVaultFake()).thenReturn(secretFake);
        when(vault.keyVaultFake()).thenReturn(keyFake);

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> new KeyVaultCertificateEntity(id.id(), input, vault));

        //then + exception
        verify(vault).secretVaultFake();
        verify(secretFake).getEntities();
        verify(secretMap).containsName(eq(id.id()));
    }

    @Test
    void testConstructorShouldGenerateMatchingVersionsWhenCalledWithValidInput() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .ips(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity entity = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        Assertions.assertEquals(entity.getId().vault(), entity.getKid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getKid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getKid().version());
        Assertions.assertEquals(entity.getId().vault(), entity.getSid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getSid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getSid().version());
    }

    @Test
    void testConstructorShouldGenerateCsrWhenCalledWithValidInput() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .ips(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final KeyVaultCertificateEntity entity = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        Assertions.assertNotNull(entity.getEncodedCertificateSigningRequest());
    }

    @Test
    void testGetEncodedCertificateSignRequestShouldReturnNullWhenCsrIsMissing() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .ips(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final KeyVaultCertificateEntity entity = spy(new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));
        doReturn(null).when(entity).getCertificateSigningRequest();

        //when
        final byte[] actual = entity.getEncodedCertificateSigningRequest();

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testGetEncodedCertificateSignRequestShouldWrapExceptionWhenErrorIsCaught() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .ips(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final KeyVaultCertificateEntity entity = spy(new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));
        doThrow(IllegalArgumentException.class).when(entity).getCertificateSigningRequest();

        //when
        Assertions.assertThrows(CryptoException.class, entity::getEncodedCertificateSigningRequest);

        //then + exception
    }

    @Test
    void testGetEncodedCertificateShouldWrapExceptionWhenErrorIsCaught() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .ips(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final KeyVaultCertificateEntity entity = spy(new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));
        doThrow(IllegalArgumentException.class).when(entity).getCertificate();

        //when
        Assertions.assertThrows(CryptoException.class, entity::getEncodedCertificate);

        //then + exception
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRenewalConstructorShouldThrowExceptionWhenCalledWithNotExistingKeyId() {
        //given
        final VersionedKeyEntityId kid = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        final VersionedCertificateEntityId id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final CertificateCreationInput input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntity(eq(kid))).thenReturn(false);

        final KeyVaultFake keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(id.vault());
        when(vault.keyVaultFake()).thenReturn(keyFake);

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> new KeyVaultCertificateEntity(input, kid, id, vault));

        //then + exception
        verify(vault).keyVaultFake();
        verify(keyFake).getEntities();
        verify(keyMap).containsEntity(eq(kid));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRenewalConstructorShouldThrowExceptionWhenNoMatchingSecretNameFound() {
        //given
        final VersionedKeyEntityId kid = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        final VersionedCertificateEntityId id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final CertificateCreationInput input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntity(eq(kid))).thenReturn(true);
        final KeyVaultFake keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final ReadOnlyVersionedEntityMultiMap<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> secretMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsName(eq(id.id()))).thenReturn(false);
        final SecretVaultFake secretFake = mock(SecretVaultFake.class);
        when(secretFake.getEntities()).thenReturn(secretMap);

        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(id.vault());
        when(vault.keyVaultFake()).thenReturn(keyFake);
        when(vault.secretVaultFake()).thenReturn(secretFake);

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> new KeyVaultCertificateEntity(input, kid, id, vault));

        //then + exception
        verify(vault).keyVaultFake();
        verify(keyFake).getEntities();
        verify(keyMap).containsEntity(eq(kid));
        verify(secretFake).getEntities();
        verify(secretMap).containsName(eq(id.id()));
    }
}
