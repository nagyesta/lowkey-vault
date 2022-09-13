package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.TestConstantsUri;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static org.mockito.Mockito.mock;

@LaunchAbortArmed
@SpringBootTest
class SecretBackupRestoreControllerIntegrationTest {

    @Autowired
    @Qualifier("SecretBackupRestoreControllerV72")
    private SecretBackupRestoreController underTest;
    @Autowired
    private VaultService vaultService;
    private URI uri;

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(mock(SecretEntityToV72ModelConverter.class), null, null))
                .add(Arguments.of(null, mock(SecretEntityToV72BackupConverter.class), null))
                .add(Arguments.of(null, null, mock(VaultService.class)))
                .add(Arguments.of(null, mock(SecretEntityToV72BackupConverter.class), null))
                .add(Arguments.of(mock(SecretEntityToV72ModelConverter.class), null, mock(VaultService.class)))
                .add(Arguments.of(mock(SecretEntityToV72ModelConverter.class), mock(SecretEntityToV72BackupConverter.class), null))
                .build();
    }

    @BeforeEach
    void setUp() {
        final String name = UUID.randomUUID().toString();
        uri = URI.create("https://" + name + ".localhost");
        vaultService.create(uri, RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, null);
    }

    @AfterEach
    void tearDown() {
        vaultService.delete(uri);
        vaultService.purge(uri);
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final SecretEntityToV72ModelConverter modelConverter,
            final SecretEntityToV72BackupConverter backupConverter,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecretBackupRestoreController(modelConverter, backupConverter, vaultService));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldRestoreASingleSecretWhenCalledWithValidInput() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_1, backupModel, TAGS_THREE_KEYS);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.restore(uri, backupModel);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultSecretModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertRestoredSecretMatchesExpectations(actualBody, SECRET_VERSION_1, TAGS_THREE_KEYS);
    }

    @Test
    void testRestoreEntityShouldRestoreAThreeSecretsWhenCalledWithValidInput() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_1, backupModel, null);
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_2, backupModel, TAGS_THREE_KEYS);
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_3, backupModel, TAGS_EMPTY);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.restore(uri, backupModel);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultSecretModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertRestoredSecretMatchesExpectations(actualBody, SECRET_VERSION_3, TAGS_EMPTY);
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithMoreThanOneUris() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_1, backupModel, null);
        addVersionToList(TestConstantsUri.HTTPS_DEFAULT_LOWKEY_VAULT, SECRET_NAME_1, SECRET_VERSION_2, backupModel, TAGS_THREE_KEYS);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithMoreThanOneNames() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_1, backupModel, null);
        addVersionToList(uri, SECRET_NAME_2, SECRET_VERSION_2, backupModel, TAGS_THREE_KEYS);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithUnknownUri() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(URI.create("https://uknknown.uri"), SECRET_NAME_1, SECRET_VERSION_1, backupModel, null);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenNameMatchesActiveSecret() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_2, backupModel, TAGS_ONE_KEY);
        vaultService.findByUri(uri).secretVaultFake().createSecretVersion(SECRET_NAME_1, LOWKEY_VAULT, null);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenNameMatchesDeletedSecret() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_2, backupModel, TAGS_ONE_KEY);
        final SecretVaultFake vaultFake = vaultService.findByUri(uri).secretVaultFake();
        final VersionedSecretEntityId secretVersion = vaultFake.createSecretVersion(SECRET_NAME_1, LOWKEY_VAULT, null);
        vaultFake.delete(secretVersion);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testBackupEntityShouldReturnTheOriginalBackupModelWhenCalledAfterRestoreEntity() {
        //given
        final SecretBackupModel backupModel = new SecretBackupModel();
        backupModel.setValue(new SecretBackupList());
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, SECRET_NAME_1, SECRET_VERSION_2, backupModel, TAGS_ONE_KEY);
        underTest.restore(uri, backupModel);

        //when
        final ResponseEntity<SecretBackupModel> actual = underTest.backup(SECRET_NAME_1, uri);

        //then
        Assertions.assertNotNull(actual);
        final SecretBackupModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(backupModel, actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

    private void assertRestoredSecretMatchesExpectations(
            final KeyVaultSecretModel actualBody, final String version, final Map<String, String> expectedTags) {
        Assertions.assertEquals(LOWKEY_VAULT, actualBody.getValue());
        Assertions.assertEquals(MimeTypeUtils.TEXT_PLAIN_VALUE, actualBody.getContentType());
        Assertions.assertEquals(new VersionedSecretEntityId(uri, SECRET_NAME_1, version).asUri(uri).toString(), actualBody.getId());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, actualBody.getAttributes().getCreatedOn());
        Assertions.assertEquals(NOW, actualBody.getAttributes().getUpdatedOn());
        Assertions.assertEquals(TIME_IN_10_MINUTES, actualBody.getAttributes().getNotBefore());
        Assertions.assertEquals(TIME_IN_10_MINUTES.plusDays(1), actualBody.getAttributes().getExpiresOn());
        Assertions.assertEquals(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, actualBody.getAttributes().getRecoveryLevel());
        Assertions.assertEquals(RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, actualBody.getAttributes().getRecoverableDays());
        Assertions.assertTrue(actualBody.getAttributes().isEnabled());
        Assertions.assertEquals(expectedTags, actualBody.getTags());
    }

    private void addVersionToList(final URI baseUri, final String name, final String version,
                                  final SecretBackupModel backupModel, final Map<String, String> tags) {
        final SecretBackupListItem listItem = new SecretBackupListItem();
        listItem.setValue(LOWKEY_VAULT);
        listItem.setContentType(MimeTypeUtils.TEXT_PLAIN_VALUE);
        listItem.setVaultBaseUri(baseUri);
        listItem.setId(name);
        listItem.setVersion(version);
        final SecretPropertiesModel propertiesModel = new SecretPropertiesModel();
        propertiesModel.setCreatedOn(TIME_10_MINUTES_AGO);
        propertiesModel.setUpdatedOn(NOW);
        propertiesModel.setNotBefore(TIME_IN_10_MINUTES);
        propertiesModel.setExpiresOn(TIME_IN_10_MINUTES.plusDays(1));
        propertiesModel.setRecoveryLevel(RecoveryLevel.RECOVERABLE_AND_PURGEABLE);
        propertiesModel.setRecoverableDays(RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE);
        listItem.setAttributes(propertiesModel);
        listItem.setTags(tags);
        final List<SecretBackupListItem> list = new ArrayList<>(backupModel.getValue().getVersions());
        list.add(listItem);
        backupModel.getValue().setVersions(list);
    }
}
