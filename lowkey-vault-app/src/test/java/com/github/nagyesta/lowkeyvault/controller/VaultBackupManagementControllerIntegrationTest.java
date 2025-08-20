package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.certificate.impl.CertContentType;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
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
        final var keyCurveName = KeyCurveName.P_256K;

        final var underTest = new VaultBackupManagementController(
                vaultImporter, vaultService, vaultImportExportExecutor);

        //when
        underTest.afterPropertiesSet();

        //then
        final var vaultFake = vaultService.findByUri(BASE_URI);
        Assertions.assertNotNull(vaultFake);
        //- keys
        final var keyVaultFake = vaultFake.keyVaultFake();
        //- older key
        final var readOnlyKeyEntityOlder = keyVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_OLDER);
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_OLDER, readOnlyKeyEntityOlder.getId());
        Assertions.assertEquals(keyCurveName, readOnlyKeyEntityOlder.keyCreationInput().getKeyParameter());
        //- younger key
        final var readOnlyKeyEntityYounger = keyVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_YOUNGER);
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_YOUNGER, readOnlyKeyEntityYounger.getId());
        Assertions.assertEquals(keyCurveName, readOnlyKeyEntityYounger.keyCreationInput().getKeyParameter());
        //- secrets
        final var secretVaultFake = vaultFake.secretVaultFake();
        final var secretEntity = secretVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_SECRET_ENTITY_ID);
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID, secretEntity.getId());
        Assertions.assertEquals(SECRET_VALUE, secretEntity.getValue());
        //- certificates
        final var certificateVaultFake = vaultFake.certificateVaultFake();
        final var certificateEntity = certificateVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_CERTIFICATE_ENTITY_ID);
        Assertions.assertEquals(VERSIONED_CERTIFICATE_ENTITY_ID, certificateEntity.getId());
        final var certificatePolicy = certificateEntity.getOriginalCertificatePolicy();
        Assertions.assertEquals(EXPECTED_SUBJECT, certificatePolicy.getSubject());
        Assertions.assertEquals(CertContentType.PKCS12, certificatePolicy.getContentType());
        Assertions.assertIterableEquals(Set.of(EXPECTED_SANS), certificatePolicy.getDnsNames());
        Assertions.assertEquals(EXPECTED_KEY_SIZE, certificatePolicy.getKeySize());
        Assertions.assertEquals(KeyType.RSA, certificatePolicy.getKeyType());
        //- managed key
        final var readOnlyKeyEntityManaged = keyVaultFake.getEntities()
                .getReadOnlyEntity(VERSIONED_CERTIFICATE_MANAGED_KEY_ENTITY_ID);
        Assertions.assertEquals(VERSIONED_CERTIFICATE_MANAGED_KEY_ENTITY_ID, readOnlyKeyEntityManaged.getId());
        Assertions.assertIterableEquals(ALL_KEY_OPERATIONS, readOnlyKeyEntityManaged.getOperations());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void testExportShouldExportVaultContentWhenCalled() {
        //given
        final var keyCurveName = KeyCurveName.P_256K;

        final var underTest = new VaultBackupManagementController(
                vaultImporter, vaultService, vaultImportExportExecutor);
        underTest.afterPropertiesSet();

        //when
        final var actual = underTest.export();

        //then
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        final var vaults = actualBody.getVaults();
        Assertions.assertEquals(1, vaults.size());
        final var vaultBackupModel = vaults.getFirst();
        //- keys
        Assertions.assertEquals(1, vaultBackupModel.getKeys().size());
        final var keyVersions = vaultBackupModel.getKeys().get(TEST_KEY).getVersions();
        Assertions.assertEquals(2, keyVersions.size());
        //- older key
        final var olderKey = keyVersions.getFirst();
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_OLDER.id(), olderKey.getId());
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_OLDER.version(), olderKey.getVersion());
        Assertions.assertEquals(keyCurveName, olderKey.getKeyMaterial().getCurveName());
        //- younger key
        final var youngerKey = keyVersions.get(1);
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_YOUNGER.id(), youngerKey.getId());
        Assertions.assertEquals(VERSIONED_KEY_ENTITY_ID_YOUNGER.version(), youngerKey.getVersion());
        Assertions.assertEquals(keyCurveName, youngerKey.getKeyMaterial().getCurveName());
        //- secrets
        Assertions.assertEquals(1, vaultBackupModel.getSecrets().size());
        final var secretVersions = vaultBackupModel.getSecrets().get(TEST_SECRET).getVersions();
        Assertions.assertEquals(1, secretVersions.size());
        final var secret = secretVersions.getFirst();
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID.id(), secret.getId());
        Assertions.assertEquals(VERSIONED_SECRET_ENTITY_ID.version(), secret.getVersion());
        Assertions.assertEquals(SECRET_VALUE, secret.getValue());
        //- certificates
        Assertions.assertEquals(1, vaultBackupModel.getCertificates().size());
        final var certificateVersions = vaultBackupModel.getCertificates().get(TEST_CERTIFICATE).getVersions();
        Assertions.assertEquals(1, certificateVersions.size());
        final var certificate = certificateVersions.getFirst();
        Assertions.assertEquals(VERSIONED_CERTIFICATE_ENTITY_ID.id(), certificate.getId());
        Assertions.assertEquals(VERSIONED_CERTIFICATE_ENTITY_ID.version(), certificate.getVersion());
        final var x509Properties = certificate.getPolicy().getX509Properties();
        final var secretProperties = certificate.getPolicy().getSecretProperties();
        final var keyProperties = certificate.getPolicy().getKeyProperties();
        Assertions.assertEquals(EXPECTED_SUBJECT, x509Properties.getSubject());
        Assertions.assertEquals(CertContentType.PKCS12.getMimeType(), secretProperties.getContentType());
        Assertions.assertIterableEquals(Set.of(EXPECTED_SANS), x509Properties.getSubjectAlternativeNames().dnsNames());
        Assertions.assertEquals(EXPECTED_KEY_SIZE, keyProperties.getKeySize());
        Assertions.assertEquals(KeyType.RSA, keyProperties.getKeyType());
    }
}
