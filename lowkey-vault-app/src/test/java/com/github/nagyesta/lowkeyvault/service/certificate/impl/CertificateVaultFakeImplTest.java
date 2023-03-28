package com.github.nagyesta.lowkeyvault.service.certificate.impl;

import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateRestoreInput;
import com.github.nagyesta.lowkeyvault.service.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.secret.impl.SecretVaultFakeImpl;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.impl.VaultFakeImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificateKeys.EMPTY_PASSWORD;
import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE;
import static com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel.RECOVERABLE_AND_PURGEABLE;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity.AUTO_RENEW;
import static com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionTriggerType.DAYS_BEFORE_EXPIRY;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.CertAuthorityType.UNKNOWN;
import static com.github.nagyesta.lowkeyvault.service.certificate.impl.KeyVaultCertificateEntityTest.VALIDITY_MONTHS;
import static org.mockito.Mockito.*;

class CertificateVaultFakeImplTest {

    public static Stream<Arguments> createNullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(CERT_NAME_1, null))
                .add(Arguments.of(null, CertificateCreationInput.builder().build()))
                .build();
    }

    public static Stream<Arguments> importNullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(CERT_NAME_1, null))
                .add(Arguments.of(null, mock(CertificateImportInput.class)))
                .build();
    }

    public static Stream<Arguments> restoreNullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(VERSIONED_CERT_ENTITY_ID_1_VERSION_1, null))
                .add(Arguments.of(null, mock(CertificateRestoreInput.class)))
                .build();
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificateVaultFakeImpl(null, null, null));

        //then + exception
    }

    @Test
    void testCreateVersionedIdShouldReturnVersionedIdWhenCalledWithValidInput() {
        //given
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = new CertificateVaultFakeImpl(vault, RecoveryLevel.PURGEABLE, null);

        //when
        final VersionedCertificateEntityId actual = underTest.createVersionedId(CERT_NAME_1, CERT_VERSION_1);

        //then
        Assertions.assertEquals(VERSIONED_CERT_ENTITY_ID_1_VERSION_1, actual);
        verify(vault).baseUri();
    }

    @Test
    void testCreateCertificateVersionShouldGenerateCertificateAndCsrWhenCalledWithValidInput() {
        //given
        final CertificateCreationInput input = CertificateCreationInput.builder()
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
                .keyUsage(Set.of(KeyUsageEnum.KEY_ENCIPHERMENT))
                .reuseKeyOnRenewal(true)
                .validityMonths(VALIDITY_MONTHS)
                .exportablePrivateKey(true)
                .build();

        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final CertificateVaultFake underTest = vault.certificateVaultFake();

        //when
        final VersionedCertificateEntityId entityId = underTest.createCertificateVersion(CERT_NAME_1, input);

        //then
        final ReadOnlyKeyVaultCertificateEntity actual = underTest.getEntities().getReadOnlyEntity(entityId);
        Assertions.assertNotNull(actual.getCertificate());
        Assertions.assertNotNull(actual.getCertificateSigningRequest());
    }

    @ParameterizedTest
    @MethodSource("createNullProvider")
    void testCreateCertificateVersionShouldThrowExceptionWhenCalledWithNull(final String name, final CertificateCreationInput input) {
        //given
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final CertificateVaultFake underTest = vault.certificateVaultFake();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.createCertificateVersion(name, input));

        //then + exception
    }

    @Test
    void testImportCertificateVersionShouldGenerateCertificateAndCsrWhenCalledWithValidPkcsInput() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsBase64String("/cert/rsa.p12"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PKCS12, new CertificatePolicyModel());
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final CertificateVaultFake underTest = vault.certificateVaultFake();

        //when
        final VersionedCertificateEntityId entityId = underTest.importCertificateVersion(CERT_NAME_1, input);

        //then
        final ReadOnlyKeyVaultCertificateEntity actual = underTest.getEntities().getReadOnlyEntity(entityId);
        Assertions.assertEquals(input.getCertificate(), actual.getCertificate());
        Assertions.assertEquals("CN=localhost", actual.getCertificateSigningRequest().getSubject().toString());
    }

    @Test
    void testImportCertificateVersionShouldGenerateCertificateAndCsrWhenCalledWithValidPemInput() {
        //given
        final String certContent = Objects.requireNonNull(ResourceUtils.loadResourceAsString("/cert/rsa.pem"));
        final CertificateImportInput input = new CertificateImportInput(
                CERT_NAME_1, certContent, EMPTY_PASSWORD, CertContentType.PEM, new CertificatePolicyModel());
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final CertificateVaultFake underTest = vault.certificateVaultFake();

        //when
        final VersionedCertificateEntityId entityId = underTest.importCertificateVersion(CERT_NAME_1, input);

        //then
        final ReadOnlyKeyVaultCertificateEntity actual = underTest.getEntities().getReadOnlyEntity(entityId);
        Assertions.assertEquals(input.getCertificate(), actual.getCertificate());
        Assertions.assertEquals("CN=localhost", actual.getCertificateSigningRequest().getSubject().toString());
    }

    @ParameterizedTest
    @MethodSource("importNullProvider")
    void testImportCertificateVersionShouldThrowExceptionWhenCalledWithNull(final String name, final CertificateImportInput input) {
        //given
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final CertificateVaultFake underTest = vault.certificateVaultFake();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.importCertificateVersion(name, input));

        //then + exception
    }

    @Test
    void testDeleteShouldPropagateDeletionToTheManagedKeyAndSecretWithTheSameNameWhenCalledWithValidInput() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);

        //when
        underTest.delete(entityId);

        //then
        verify(vault, atLeastOnce()).baseUri();
        verify(vault, atLeastOnce()).secretVaultFake();
        verify(vault, atLeastOnce()).keyVaultFake();
        Assertions.assertTrue(vault.certificateVaultFake().getEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.certificateVaultFake().getDeletedEntities().containsName(CERT_NAME_1));
        Assertions.assertTrue(vault.keyVaultFake().getEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.keyVaultFake().getDeletedEntities().containsName(CERT_NAME_1));
        Assertions.assertTrue(vault.secretVaultFake().getEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.secretVaultFake().getDeletedEntities().containsName(CERT_NAME_1));
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testDeleteShouldShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.delete(null));

        //then + exception
    }

    @Test
    void testPurgeShouldPropagateDeletionToTheManagedKeyAndSecretWithTheSameNameWhenCalledWithValidInput() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);
        underTest.delete(entityId);

        //when
        underTest.purge(entityId);

        //then
        verify(vault, atLeastOnce()).baseUri();
        verify(vault, atLeastOnce()).secretVaultFake();
        verify(vault, atLeastOnce()).keyVaultFake();
        Assertions.assertTrue(vault.certificateVaultFake().getEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.certificateVaultFake().getDeletedEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.keyVaultFake().getEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.keyVaultFake().getDeletedEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.secretVaultFake().getEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.secretVaultFake().getDeletedEntities().listLatestEntities().isEmpty());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testPurgeShouldShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);
        underTest.delete(entityId);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.purge(null));

        //then + exception
    }

    @Test
    void testRecoverShouldPropagateDeletionToTheManagedKeyAndSecretWithTheSameNameWhenCalledWithValidInput() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);
        underTest.delete(entityId);

        //when
        underTest.recover(entityId);

        //then
        verify(vault, atLeastOnce()).baseUri();
        verify(vault, atLeastOnce()).secretVaultFake();
        verify(vault, atLeastOnce()).keyVaultFake();
        Assertions.assertTrue(vault.certificateVaultFake().getEntities().containsName(CERT_NAME_1));
        Assertions.assertTrue(vault.certificateVaultFake().getDeletedEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.keyVaultFake().getEntities().containsName(CERT_NAME_1));
        Assertions.assertTrue(vault.keyVaultFake().getDeletedEntities().listLatestEntities().isEmpty());
        Assertions.assertTrue(vault.secretVaultFake().getEntities().containsName(CERT_NAME_1));
        Assertions.assertTrue(vault.secretVaultFake().getDeletedEntities().listLatestEntities().isEmpty());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testRecoverShouldShouldThrowExceptionWhenCalledWithNull() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);
        underTest.delete(entityId);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.recover(null));

        //then + exception
    }

    @Test
    void testSetLifetimeActionPolicyShouldStorePolicyWhenNoPolicyExistsYet() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> map = Map
                .of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 1));
        final CertificateLifetimeActionPolicy policy = new CertificateLifetimeActionPolicy(entityId, map);

        //when
        underTest.setLifetimeActionPolicy(policy);

        //then
        final LifetimeActionPolicy actual = underTest.lifetimeActionPolicy(entityId);
        Assertions.assertEquals(policy.getLifetimeActions(), actual.getLifetimeActions());
    }

    @Test
    void testSetLifetimeActionPolicyShouldReplaceActionsInPolicyObjectWhenCalledWithAlreadyExistingPolicy() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> map = Map
                .of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 1));
        final CertificateLifetimeActionPolicy policy = new CertificateLifetimeActionPolicy(entityId, Map.of());
        underTest.setLifetimeActionPolicy(policy);
        final LifetimeActionPolicy original = underTest.lifetimeActionPolicy(entityId);

        //when
        underTest.setLifetimeActionPolicy(new CertificateLifetimeActionPolicy(entityId, map));

        //then
        final LifetimeActionPolicy actual = underTest.lifetimeActionPolicy(entityId);
        Assertions.assertSame(original, actual);
        Assertions.assertEquals(policy.getLifetimeActions(), actual.getLifetimeActions());
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testSetLifetimeActionPolicyShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.setLifetimeActionPolicy(null));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testLifetimeActionPolicyShouldThrowExceptionWhenCalledWithNull() {
        //given
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.lifetimeActionPolicy(null));

        //then + exception
    }

    @Test
    void testLifetimeActionPolicyShouldPurgePolicyWhenEntityIsPurgedAlready() {
        //given
        final CertificateEntityId entityId = new CertificateEntityId(HTTPS_LOCALHOST_8443, CERT_NAME_1);
        final VaultFake vault = mock(VaultFake.class);
        when(vault.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        final CertificateVaultFakeImpl underTest = prepareWithCertificateCreated(vault);
        final Map<CertificateLifetimeActionActivity, CertificateLifetimeActionTrigger> map = Map
                .of(AUTO_RENEW, new CertificateLifetimeActionTrigger(DAYS_BEFORE_EXPIRY, 1));
        final CertificateLifetimeActionPolicy policy = new CertificateLifetimeActionPolicy(entityId, map);
        underTest.setLifetimeActionPolicy(policy);
        underTest.delete(entityId);
        Assertions.assertNotNull(underTest.lifetimeActionPolicy(entityId));
        underTest.purge(entityId);

        //when
        final LifetimeActionPolicy actual = underTest.lifetimeActionPolicy(entityId);

        //then
        Assertions.assertNull(actual);
    }

    @ParameterizedTest
    @MethodSource("restoreNullProvider")
    void testRestoreCertificateVersionShouldThrowExceptionWhenCalledWithNull(
            final VersionedCertificateEntityId id, final CertificateRestoreInput input) {
        //given
        final VaultFake vault = new VaultFakeImpl(HTTPS_LOCALHOST_8443);
        final CertificateVaultFake underTest = vault.certificateVaultFake();

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restoreCertificateVersion(id, input));

        //then + exception
    }

    private CertificateVaultFakeImpl prepareWithCertificateCreated(final VaultFake vault) {
        final CertificateCreationInput cert = CertificateCreationInput.builder()
                .subject("CN=" + LOCALHOST)
                .name(CERT_NAME_1)
                .keyType(KeyType.EC)
                .keyCurveName(KeyCurveName.P_256)
                .validityStart(NOW)
                .validityMonths(VALIDITY_MONTHS)
                .contentType(CertContentType.PEM)
                .build();
        when(vault.getRecoveryLevel()).thenReturn(RECOVERABLE_AND_PURGEABLE);
        when(vault.getRecoverableDays()).thenReturn(MAX_RECOVERABLE_DAYS_INCLUSIVE);
        final KeyVaultFakeImpl keyVaultFake = new KeyVaultFakeImpl(
                vault, RECOVERABLE_AND_PURGEABLE, MAX_RECOVERABLE_DAYS_INCLUSIVE);
        when(vault.keyVaultFake()).thenReturn(keyVaultFake);
        final SecretVaultFakeImpl secretVaultFake = new SecretVaultFakeImpl(
                vault, RECOVERABLE_AND_PURGEABLE, MAX_RECOVERABLE_DAYS_INCLUSIVE);
        when(vault.secretVaultFake()).thenReturn(secretVaultFake);
        final CertificateVaultFakeImpl underTest = new CertificateVaultFakeImpl(
                vault, RECOVERABLE_AND_PURGEABLE, MAX_RECOVERABLE_DAYS_INCLUSIVE);
        when(vault.certificateVaultFake()).thenReturn(underTest);
        underTest.createCertificateVersion(CERT_NAME_1, cert);
        return underTest;
    }
}
