package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateRestoreInput;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
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

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_RSA_KEY_SIZE;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation.*;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.UNKNOWN;
import static org.mockito.Mockito.*;

class KeyVaultCertificateEntityTest {

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
        final var kid = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        final var cid = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var vaultFake = mock(VaultFake.class);
        final var input = CertificateCreationInput.builder().build();
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

    public static Stream<Arguments> nullRestoreProvider() {
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var input = mock(CertificateRestoreInput.class);
        final var vault = mock(VaultFake.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(id, null, null))
                .add(Arguments.of(null, input, null))
                .add(Arguments.of(null, null, vault))
                .add(Arguments.of(null, input, vault))
                .add(Arguments.of(id, null, vault))
                .add(Arguments.of(id, input, null))
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
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntityMatching(eq(id.id()), any())).thenReturn(true);

        final var keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final var vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(id.vault());
        when(vault.getRecoveryLevel()).thenReturn(RecoveryLevel.PURGEABLE);
        when(vault.getRecoverableDays()).thenReturn(null);
        when(vault.keyVaultFake()).thenReturn(keyFake);

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> new KeyVaultCertificateEntity(id.id(), input, vault));

        //then + exception
        verify(vault).keyVaultFake();
        verify(keyFake).getEntities();
        verify(keyMap).containsEntityMatching(eq(id.id()), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithAlreadyUsedSecretName() {
        //given
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> secretMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(secretMap.containsEntityMatching(eq(id.id()), any())).thenReturn(true);

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);

        final var secretFake = mock(SecretVaultFake.class);
        when(secretFake.getEntities()).thenReturn(secretMap);
        final var keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final var vault = mock(VaultFake.class);
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
        verify(secretMap).containsEntityMatching(eq(id.id()), any());
    }

    @Test
    void testConstructorShouldGenerateMatchingVersionsWhenCalledWithValidInput() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.DIGITAL_SIGNATURE))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS_ONE_YEAR)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final var entity = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        Assertions.assertEquals(entity.getId().vault(), entity.getKid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getKid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getKid().version());
        Assertions.assertEquals(entity.getId().vault(), entity.getSid().vault());
        Assertions.assertEquals(entity.getId().id(), entity.getSid().id());
        Assertions.assertEquals(entity.getId().version(), entity.getSid().version());
    }

    @Test
    void testConstructorShouldSetKeyOperationsWhenCalledWithValidEcInput() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.DIGITAL_SIGNATURE))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS_ONE_YEAR)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final var entity = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        final var actual = vault.keyVaultFake()
                .getEntities()
                .getEntity(entity.getKid(), EcKeyVaultKeyEntity.class)
                .getOperations();
        Assertions.assertIterableEquals(List.of(SIGN, VERIFY), actual);
    }

    @Test
    void testConstructorShouldSetKeyOperationsWhenCalledWithValidRsaInput() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.RSA)
                .keySize(MIN_RSA_KEY_SIZE)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS_ONE_YEAR)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final var entity = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        final var actual = vault.keyVaultFake()
                .getEntities()
                .getEntity(entity.getKid(), RsaKeyVaultKeyEntity.class)
                .getOperations();
        Assertions.assertIterableEquals(List.of(ENCRYPT, DECRYPT, WRAP_KEY, UNWRAP_KEY), actual);
    }

    @Test
    void testConstructorShouldGenerateCsrWhenCalledWithValidInput() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.DIGITAL_SIGNATURE))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS_ONE_YEAR)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);

        //when
        final var entity = new KeyVaultCertificateEntity(CERT_NAME_1, input, vault);

        //then
        Assertions.assertNotNull(entity.getEncodedCertificateSigningRequest());
    }

    @Test
    void testGetEncodedCertificateSignRequestShouldReturnNullWhenCsrIsMissing() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.DIGITAL_SIGNATURE))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS_ONE_YEAR)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final var entity = spy(new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));
        doReturn(null).when(entity).getCertificateSigningRequest();

        //when
        final var actual = entity.getEncodedCertificateSigningRequest();

        //then
        Assertions.assertNull(actual);
    }

    @Test
    void testGetEncodedCertificateSignRequestShouldWrapExceptionWhenErrorIsCaught() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.DIGITAL_SIGNATURE))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS_ONE_YEAR)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final var entity = spy(new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));
        doThrow(IllegalArgumentException.class).when(entity).getCertificateSigningRequest();

        //when
        Assertions.assertThrows(CryptoException.class, entity::getEncodedCertificateSigningRequest);

        //then + exception
    }

    @Test
    void testGetEncodedCertificateShouldWrapExceptionWhenErrorIsCaught() {
        //given
        final var input = CertificateCreationInput.builder()
                .validityStart(NOW)
                .subject("CN=" + LOCALHOST)
                .upns(Set.of(LOOP_BACK_IP))
                .name(CERT_NAME_1)
                .dnsNames(Set.of(LOWKEY_VAULT))
                .enableTransparency(false)
                .certAuthorityType(UNKNOWN)
                .contentType(CertContentType.PEM)
                .certificateType(null)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .extendedKeyUsage(Set.of("1.3.6.1.5.5.7.3.1", "1.3.6.1.5.5.7.3.2"))
                .keyUsage(Set.of(KeyUsageEnum.DIGITAL_SIGNATURE))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS_ONE_YEAR)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final var entity = spy(new KeyVaultCertificateEntity(CERT_NAME_1, input, vault));
        doThrow(IllegalArgumentException.class).when(entity).getCertificate();

        //when
        Assertions.assertThrows(CryptoException.class, entity::getEncodedCertificate);

        //then + exception
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRenewalConstructorShouldThrowExceptionWhenCalledWithNotExistingKeyId() {
        //given
        final var kid = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntity(eq(kid))).thenReturn(false);

        final var keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final var vault = mock(VaultFake.class);
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
        final var kid = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var input = CertificateCreationInput.builder().name(id.id()).build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntity(eq(kid))).thenReturn(true);
        final var keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final ReadOnlyVersionedEntityMultiMap<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> secretMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntityMatching(eq(id.id()), any())).thenReturn(false);
        final var secretFake = mock(SecretVaultFake.class);
        when(secretFake.getEntities()).thenReturn(secretMap);

        final var vault = mock(VaultFake.class);
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
        verify(secretMap).containsEntityMatching(eq(id.id()), any());
    }

    @ParameterizedTest
    @MethodSource("nullRestoreProvider")
    void testRestoreConstructorShouldThrowExceptionWhenCalledWithNull(
            final VersionedCertificateEntityId id, final CertificateRestoreInput input, final VaultFake vault) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new KeyVaultCertificateEntity(id, input, vault));

        //then + exception
    }
}
