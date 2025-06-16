package com.github.nagyesta.lowkeyvault.controller.v7_6;

import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateLifetimeActionActivity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.CertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.MIN_RSA_KEY_SIZE;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUri;

@SpringBootTest
class CertificateBackupRestoreControllerIntegrationTest {

    @Autowired
    private VaultService vaultService;
    @Autowired
    private CertificateController certificateController;
    @Autowired
    @Qualifier("certificateBackupRestoreControllerV76")
    private CertificateBackupRestoreController underTest;

    public static Stream<Arguments> certTypeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(CertContentType.PEM, getRandomVaultUri()))
                .add(Arguments.of(CertContentType.PKCS12, getRandomVaultUri()))
                .build();
    }

    @ParameterizedTest
    @MethodSource("certTypeProvider")
    void testRestoreShouldRestoreOriginalCertificateAndSecretValuesWhenCalledWithValidBackupOutput(
            final CertContentType contentType, final URI baseUri) {
        //given
        vaultService.create(baseUri,
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, Set.of());
        final var creteRequest = prepareCertificateCreateRequest(contentType);

        certificateController.create(CERTIFICATE_BACKUP_TEST, baseUri, creteRequest);
        final var vaultFake = vaultService.findByUri(baseUri);
        vaultFake.timeShift(SECONDS_IN_FIVE_DAYS, true);

        final var entityId = new CertificateEntityId(baseUri, CERTIFICATE_BACKUP_TEST);
        final var versions = vaultFake.certificateVaultFake().getEntities().getVersions(entityId);
        final var expectedCerts = getAllCertificateModelVersions(baseUri, versions);
        final var expectedSecrets = getAllSecretValuesForVersions(entityId, vaultFake, versions);
        final var expectedKey = getOnlyKeyForVersions(entityId, vaultFake, versions);

        //when
        final var backupModel = underTest.backup(CERTIFICATE_BACKUP_TEST, baseUri).getBody();
        Assertions.assertNotNull(backupModel);
        vaultService.delete(baseUri);
        vaultService.purge(baseUri);
        vaultService.create(baseUri,
                RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, Set.of());
        final var restored = underTest.restore(baseUri, backupModel);

        //then
        Assertions.assertNotNull(restored);
        Assertions.assertEquals(HttpStatus.OK, restored.getStatusCode());
        Assertions.assertNotNull(restored.getBody());
        Assertions.assertEquals(expectedCerts.get(1), restored.getBody());
        final var actualCerts = getAllCertificateModelVersions(baseUri, versions);
        final var actualSecrets = getAllSecretValuesForVersions(entityId, vaultFake, versions);
        final var actualKey = getOnlyKeyForVersions(entityId, vaultFake, versions);
        Assertions.assertIterableEquals(expectedCerts, actualCerts);
        Assertions.assertIterableEquals(expectedSecrets, actualSecrets);
        Assertions.assertEquals(expectedKey, actualKey);
    }

    private static CreateCertificateRequest prepareCertificateCreateRequest(final CertContentType contentType) {
        final var creteRequest = new CreateCertificateRequest();
        final var policy = new CertificatePolicyModel();
        final var x509 = new X509CertificateModel();
        x509.setSubject(CN_TEST);
        x509.setValidityMonths(VALIDITY_TEN_MONTHS);
        x509.setKeyUsage(KEY_USAGE_ENCIPHER_ONLY_DECIPHER_ONLY);
        x509.setExtendedKeyUsage(EXTENDED_KEY_USAGE);
        x509.setSubjectAlternativeNames(new SubjectAlternativeNames(SANS_TEST_COM_AND_TEST2_COM, Set.of(), Set.of()));
        policy.setX509Properties(x509);

        final var issuer = new IssuerParameterModel();
        issuer.setIssuer(SELF);
        issuer.setCertTransparency(CERT_TRANSPARENCY);
        issuer.setCertType(SELF_SIGNED);
        policy.setIssuer(issuer);

        final var attributes = new CertificatePropertiesModel();
        attributes.setEnabled(ENABLED);
        policy.setAttributes(attributes);

        final var secret = new CertificateSecretModel();
        secret.setContentType(contentType.getMimeType());
        policy.setSecretProperties(secret);

        final var key = new CertificateKeyModel();
        key.setKeySize(MIN_RSA_KEY_SIZE);
        key.setReuseKey(REUSE_KEY);
        key.setKeyType(KeyType.RSA);
        key.setExportable(EXPORTABLE);
        policy.setKeyProperties(key);

        final var lifetimeAction = new CertificateLifetimeActionModel();
        lifetimeAction.setAction(CertificateLifetimeActionActivity.AUTO_RENEW);
        final var trigger = new CertificateLifetimeActionTriggerModel();
        trigger.setLifetimePercentage(1);
        lifetimeAction.setTrigger(trigger);
        policy.setLifetimeActions(List.of(lifetimeAction));
        creteRequest.setPolicy(policy);
        return creteRequest;
    }

    private static List<String> getAllSecretValuesForVersions(
            final CertificateEntityId entityId, final VaultFake vaultFake, final Deque<String> versions) {
        final var secrets = versions.stream()
                .map(v -> new VersionedSecretEntityId(entityId.vault(), entityId.id(), v))
                .map(vaultFake.secretVaultFake().getEntities()::getReadOnlyEntity)
                .map(ReadOnlyKeyVaultSecretEntity::getValue)
                .toList();
        Assertions.assertEquals(2, secrets.size());
        return secrets;
    }

    private static ReadOnlyKeyVaultKeyEntity getOnlyKeyForVersions(
            final CertificateEntityId entityId, final VaultFake vaultFake, final Deque<String> versions) {
        final var keys = versions.stream()
                .map(v -> new VersionedKeyEntityId(entityId.vault(), entityId.id(), v))
                .filter(vaultFake.keyVaultFake().getEntities()::containsEntity)
                .map(vaultFake.keyVaultFake().getEntities()::getReadOnlyEntity)
                .toList();
        Assertions.assertEquals(1, keys.size());
        return keys.get(0);
    }

    private List<KeyVaultCertificateModel> getAllCertificateModelVersions(
            final URI baseUri, final Deque<String> versions) {
        final var certs = versions.stream()
                .map(v -> certificateController.getWithVersion(CERTIFICATE_BACKUP_TEST, v, baseUri).getBody())
                .toList();
        Assertions.assertEquals(2, certs.size());
        return certs;
    }
}
