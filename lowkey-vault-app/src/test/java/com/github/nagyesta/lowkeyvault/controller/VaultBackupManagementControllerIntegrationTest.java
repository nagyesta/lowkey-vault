package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupListItem;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificateSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.X509CertificateModel;
import com.github.nagyesta.lowkeyvault.service.certificate.CertificateVaultFake;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.ReadOnlyCertificatePolicy;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.TestConstantsCertificates.ALL_KEY_OPERATIONS;

@LaunchAbortArmed
@SpringBootTest(properties = {"LOWKEY_VAULT_NAMES=-"}, classes = VaultBackupConfiguration.class)
@ActiveProfiles("vault")
class VaultBackupManagementControllerIntegrationTest {

    private static final URI BASE_URI = URI.create("https://127.0.0.1:8444");
    private static final String TEST_KEY = "test-key";
    private static final String TEST_SECRET = "test-secret";
    private static final String TEST_CERTIFICATE = "rsa-cert";
    private static final String KEY_VERSION_1 = "c1b1abd7f3494996b22a6920589581e9";
    private static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_OLDER =
            new VersionedKeyEntityId(BASE_URI, TEST_KEY, KEY_VERSION_1);
    private static final String KEY_VERSION_2 = "b578bbb88e464576be39eb795f209a47";
    private static final VersionedKeyEntityId VERSIONED_KEY_ENTITY_ID_YOUNGER =
            new VersionedKeyEntityId(BASE_URI, TEST_KEY, KEY_VERSION_2);
    private static final String SECRET_VERSION = "387e6991114c67cd9c8a7f7c8eb2f469";
    private static final VersionedSecretEntityId VERSIONED_SECRET_ENTITY_ID =
            new VersionedSecretEntityId(BASE_URI, TEST_SECRET, SECRET_VERSION);
    private static final String CERTIFICATE_VERSION = "6ae160e0bddc486691653798e41abee0";
    private static final VersionedCertificateEntityId VERSIONED_CERTIFICATE_ENTITY_ID =
            new VersionedCertificateEntityId(BASE_URI, TEST_CERTIFICATE, CERTIFICATE_VERSION);
    private static final VersionedKeyEntityId VERSIONED_CERTIFICATE_MANAGED_KEY_ENTITY_ID =
            new VersionedKeyEntityId(BASE_URI, TEST_CERTIFICATE, CERTIFICATE_VERSION);
    private static final String SECRET_VALUE = "$3cret";
    private static final int EXPECTED_KEY_SIZE = 2048;
    private static final String EXPECTED_SANS = "*.example.com";
    private static final String EXPECTED_SUBJECT = "CN=example.com";

    @Autowired
    private VaultService vaultService;
    @Autowired
    private VaultImporter vaultImporter;
    @Autowired
    private VaultImportExportExecutor vaultImportExportExecutor;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testAfterPropertiesSetShouldParseAndImportVaultsWhenCalled() {
        //given
        final KeyCurveName keyCurveName = KeyCurveName.P_256K;

        final VaultBackupManagementController underTest = new VaultBackupManagementController(
                vaultImporter, vaultService, vaultImportExportExecutor);

        //when
        underTest.afterPropertiesSet();

        //then
        final VaultFake vaultFake = vaultService.findByUri(BASE_URI);
        Assertions.assertNotNull(vaultFake);
        //- keys
        final KeyVaultFake keyVaultFake = vaultFake.keyVaultFake();
        //- older key
        final ReadOnlyKeyVaultKeyEntity readOnlyKeyEntityOlder = keyVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_OLDER);
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_OLDER, readOnlyKeyEntityOlder.getId());
        Assertions.assertEquals(keyCurveName, readOnlyKeyEntityOlder.keyCreationInput().getKeyParameter());
        //- younger key
        final ReadOnlyKeyVaultKeyEntity readOnlyKeyEntityYounger = keyVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_YOUNGER);
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_YOUNGER, readOnlyKeyEntityYounger.getId());
        Assertions.assertEquals(keyCurveName, readOnlyKeyEntityYounger.keyCreationInput().getKeyParameter());
        //- secrets
        final SecretVaultFake secretVaultFake = vaultFake.secretVaultFake();
        final ReadOnlyKeyVaultSecretEntity secretEntity = secretVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_SECRET_ENTITY_ID);
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID, secretEntity.getId());
        Assertions.assertEquals(SECRET_VALUE, secretEntity.getValue());
        //- certificates
        final CertificateVaultFake certificateVaultFake = vaultFake.certificateVaultFake();
        final ReadOnlyKeyVaultCertificateEntity certificateEntity = certificateVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_CERTIFICATE_ENTITY_ID);
        Assertions.assertEquals(VERSIONED_CERTIFICATE_ENTITY_ID, certificateEntity.getId());
        final ReadOnlyCertificatePolicy certificatePolicy = certificateEntity.getOriginalCertificatePolicy();
        Assertions.assertEquals(EXPECTED_SUBJECT, certificatePolicy.getSubject());
        Assertions.assertEquals(CertContentType.PKCS12, certificatePolicy.getContentType());
        Assertions.assertIterableEquals(Set.of(EXPECTED_SANS), certificatePolicy.getDnsNames());
        Assertions.assertEquals(EXPECTED_KEY_SIZE, certificatePolicy.getKeySize());
        Assertions.assertEquals(KeyType.RSA, certificatePolicy.getKeyType());
        //- managed key
        final ReadOnlyKeyVaultKeyEntity readOnlyKeyEntityManaged = keyVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_CERTIFICATE_MANAGED_KEY_ENTITY_ID);
        Assertions.assertEquals(VERSIONED_CERTIFICATE_MANAGED_KEY_ENTITY_ID, readOnlyKeyEntityManaged.getId());
        Assertions.assertIterableEquals(ALL_KEY_OPERATIONS, readOnlyKeyEntityManaged.getOperations());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testExportShouldExportVaultContentWhenCalled() {
        //given
        final KeyCurveName keyCurveName = KeyCurveName.P_256K;

        final VaultBackupManagementController underTest = new VaultBackupManagementController(
                vaultImporter, vaultService, vaultImportExportExecutor);
        underTest.afterPropertiesSet();

        //when
        final ResponseEntity<VaultBackupListModel> actual = underTest.export();

        //then
        final VaultBackupListModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        final List<VaultBackupModel> vaults = actualBody.getVaults();
        Assertions.assertEquals(1, vaults.size());
        final VaultBackupModel vaultBackupModel = vaults.get(0);
        //- keys
        Assertions.assertEquals(1, vaultBackupModel.getKeys().size());
        final List<KeyBackupListItem> keyVersions = vaultBackupModel.getKeys().get(TEST_KEY).getVersions();
        Assertions.assertEquals(2, keyVersions.size());
        //- older key
        final KeyBackupListItem olderKey = keyVersions.get(0);
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_OLDER.id(), olderKey.getId());
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_OLDER.version(), olderKey.getVersion());
        Assertions.assertEquals(keyCurveName, olderKey.getKeyMaterial().getCurveName());
        //- younger key
        final KeyBackupListItem youngerKey = keyVersions.get(1);
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_YOUNGER.id(), youngerKey.getId());
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_YOUNGER.version(), youngerKey.getVersion());
        Assertions.assertEquals(keyCurveName, youngerKey.getKeyMaterial().getCurveName());
        //- secrets
        Assertions.assertEquals(1, vaultBackupModel.getSecrets().size());
        final List<SecretBackupListItem> secretVersions = vaultBackupModel.getSecrets().get(TEST_SECRET).getVersions();
        Assertions.assertEquals(1, secretVersions.size());
        final SecretBackupListItem secret = secretVersions.get(0);
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID.id(), secret.getId());
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID.version(), secret.getVersion());
        Assertions.assertEquals(SECRET_VALUE, secret.getValue());
        //- certificates
        Assertions.assertEquals(1, vaultBackupModel.getCertificates().size());
        final List<CertificateBackupListItem> certificateVersions = vaultBackupModel.getCertificates().get(TEST_CERTIFICATE).getVersions();
        Assertions.assertEquals(1, certificateVersions.size());
        final CertificateBackupListItem certificate = certificateVersions.get(0);
        Assertions.assertEquals(VERSIONED_CERTIFICATE_ENTITY_ID.id(), certificate.getId());
        Assertions.assertEquals(VERSIONED_CERTIFICATE_ENTITY_ID.version(), certificate.getVersion());
        final X509CertificateModel x509Properties = certificate.getPolicy().getX509Properties();
        final CertificateSecretModel secretProperties = certificate.getPolicy().getSecretProperties();
        final CertificateKeyModel keyProperties = certificate.getPolicy().getKeyProperties();
        Assertions.assertEquals(EXPECTED_SUBJECT, x509Properties.getSubject());
        Assertions.assertEquals(CertContentType.PKCS12.getMimeType(), secretProperties.getContentType());
        Assertions.assertIterableEquals(Set.of(EXPECTED_SANS), x509Properties.getSubjectAlternativeNames().dnsNames());
        Assertions.assertEquals(EXPECTED_KEY_SIZE, keyProperties.getKeySize());
        Assertions.assertEquals(KeyType.RSA, keyProperties.getKeyType());
    }
}
