package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
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

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_RSA_KEY_SIZE;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation.*;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.UNKNOWN;
import static org.mockito.Mockito.*;

class KeyVaultCertificateEntityTest {

    @SuppressWarnings("unchecked")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithAlreadyUsedKeyName() {
        //given
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var name = id.id();
        final var input = CertificateCreationInput.builder()
                .name(name)
                .certAuthorityType(CertAuthorityType.SELF_SIGNED)
                .subject("CN=localhost")
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .validityStart(OffsetDateTime.now())
                .contentType(CertContentType.PEM)
                .build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntityMatching(eq(name), any())).thenReturn(true);

        final var keyFake = mock(KeyVaultFake.class);
        when(keyFake.getEntities()).thenReturn(keyMap);

        final var vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(id.vault());
        when(vault.getRecoveryLevel()).thenReturn(RecoveryLevel.PURGEABLE);
        when(vault.getRecoverableDays()).thenReturn(null);
        when(vault.keyVaultFake()).thenReturn(keyFake);

        //when
        Assertions.assertThrows(IllegalStateException.class, () -> new KeyVaultCertificateEntity(name, input, vault));

        //then + exception
        verify(vault).keyVaultFake();
        verify(keyFake).getEntities();
        verify(keyMap).containsEntityMatching(eq(name), any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithAlreadyUsedSecretName() {
        //given
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var name = id.id();
        final var input = CertificateCreationInput.builder()
                .name(name)
                .certAuthorityType(CertAuthorityType.SELF_SIGNED)
                .subject("CN=localhost")
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .validityStart(OffsetDateTime.now())
                .contentType(CertContentType.PEM)
                .build();

        final ReadOnlyVersionedEntityMultiMap<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> secretMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(secretMap.containsEntityMatching(eq(name), any())).thenReturn(true);

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
        Assertions.assertThrows(IllegalStateException.class, () -> new KeyVaultCertificateEntity(name, input, vault));

        //then + exception
        verify(vault).secretVaultFake();
        verify(secretFake).getEntities();
        verify(secretMap).containsEntityMatching(eq(name), any());
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
        final var input = CertificateCreationInput.builder()
                .name(id.id())
                .certAuthorityType(CertAuthorityType.SELF_SIGNED)
                .subject("CN=localhost")
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .validityStart(OffsetDateTime.now())
                .contentType(CertContentType.PEM)
                .build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntity(kid)).thenReturn(false);

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
        verify(keyMap).containsEntity(kid);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testRenewalConstructorShouldThrowExceptionWhenNoMatchingSecretNameFound() {
        //given
        final var kid = VERSIONED_KEY_ENTITY_ID_1_VERSION_1;
        final var id = VERSIONED_CERT_ENTITY_ID_1_VERSION_1;
        final var input = CertificateCreationInput.builder()
                .name(id.id())
                .certAuthorityType(CertAuthorityType.SELF_SIGNED)
                .subject("CN=localhost")
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_521)
                .validityStart(OffsetDateTime.now())
                .contentType(CertContentType.PEM)
                .build();

        final ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> keyMap
                = mock(ReadOnlyVersionedEntityMultiMap.class);
        when(keyMap.containsEntity(kid)).thenReturn(true);
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
        verify(keyMap).containsEntity(kid);
        verify(secretFake).getEntities();
        verify(secretMap).containsEntityMatching(eq(id.id()), any());
    }
}
