package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.TestConstantsUri;
import com.github.nagyesta.lowkeyvault.controller.BaseKeyBackupRestoreControllerIntegrationTest;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.impl.EcKeyCreationInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreateDetailedInput;
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

import java.net.URI;
import java.security.interfaces.ECPublicKey;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static org.mockito.Mockito.mock;

@LaunchAbortArmed
@SpringBootTest
class KeyBackupRestoreControllerIntegrationTest extends BaseKeyBackupRestoreControllerIntegrationTest {

    @Autowired
    @Qualifier("keyBackupRestoreControllerV72")
    private KeyBackupRestoreController underTest;
    @Autowired
    private VaultService vaultService;

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(mock(KeyConverterRegistry.class), null))
                .add(Arguments.of(null, mock(VaultService.class)))
                .build();
    }

    @BeforeEach
    void setUp() {
        final var name = UUID.randomUUID().toString();
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
            final KeyConverterRegistry registry,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyBackupRestoreController(registry, vaultService));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldRestoreASingleKeyWhenCalledWithValidInput() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        final var expectedKey = addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_THREE_KEYS);

        //when
        final var actual = underTest.restore(uri, backupModel);

        //then
        Assertions.assertNotNull(actual);
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertRestoredKeyMatchesExpectations(actualBody, (ECPublicKey) expectedKey.getPublic(), KEY_VERSION_1, TAGS_THREE_KEYS);
    }

    @Test
    void testRestoreEntityShouldRestoreThreeKeysWhenCalledWithValidInput() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, null);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_THREE_KEYS);
        final var expectedKey = addVersionToList(uri, KEY_NAME_1, KEY_VERSION_3, backupModel, TAGS_EMPTY);

        //when
        final var actual = underTest.restore(uri, backupModel);

        //then
        Assertions.assertNotNull(actual);
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertRestoredKeyMatchesExpectations(actualBody, (ECPublicKey) expectedKey.getPublic(), KEY_VERSION_3, TAGS_EMPTY);
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithMoreThanOneUris() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, null);
        addVersionToList(TestConstantsUri.HTTPS_DEFAULT_LOWKEY_VAULT, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_THREE_KEYS);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithMoreThanOneNames() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, null);
        addVersionToList(uri, KEY_NAME_2, KEY_VERSION_2, backupModel, TAGS_THREE_KEYS);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenCalledWithUnknownUri() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(URI.create("https://uknknown.uri"), KEY_NAME_1, KEY_VERSION_1, backupModel, null);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenNameMatchesActiveKey() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_ONE_KEY);
        vaultService.findByUri(uri).keyVaultFake()
                .createKeyVersion(KEY_NAME_1, KeyCreateDetailedInput.builder()
                        .key(new EcKeyCreationInput(KeyType.EC, KeyCurveName.P_256))
                        .build());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testRestoreEntityShouldThrowExceptionWhenNameMatchesDeletedKey() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_ONE_KEY);
        final var vaultFake = vaultService.findByUri(uri).keyVaultFake();
        final var keyVersion = vaultFake
                .createKeyVersion(KEY_NAME_1, KeyCreateDetailedInput.builder()
                        .key(new EcKeyCreationInput(KeyType.EC, KeyCurveName.P_256))
                        .build());
        vaultFake.delete(keyVersion);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restore(uri, backupModel));

        //then + exception
    }

    @Test
    void testBackupEntityShouldReturnTheOriginalBackupModelWhenCalledAfterRestoreEntity() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_EMPTY);
        addVersionToList(uri, KEY_NAME_1, KEY_VERSION_2, backupModel, TAGS_ONE_KEY);
        underTest.restore(uri, backupModel);

        //when
        final var actual = underTest.backup(KEY_NAME_1, uri);

        //then
        Assertions.assertNotNull(actual);
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(backupModel, actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
    }

}
