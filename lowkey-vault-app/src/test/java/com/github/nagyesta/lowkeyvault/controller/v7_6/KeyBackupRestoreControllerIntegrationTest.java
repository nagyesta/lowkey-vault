package com.github.nagyesta.lowkeyvault.controller.v7_6;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.TestConstantsUri;
import com.github.nagyesta.lowkeyvault.controller.BaseKeyBackupRestoreControllerIntegrationTest;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.*;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.constants.LifetimeActionTriggerType;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
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
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.getRandomVaultUri;
import static com.github.nagyesta.lowkeyvault.model.v7_3.key.constants.LifetimeActionType.ROTATE;
import static org.mockito.Mockito.mock;

@LaunchAbortArmed
@SpringBootTest
class KeyBackupRestoreControllerIntegrationTest extends BaseKeyBackupRestoreControllerIntegrationTest {

    private static final Period EXPIRY_TIME = Period.ofDays(LifetimeActionTriggerType.MINIMUM_EXPIRY_PERIOD_IN_DAYS);
    private static final Period TRIGGER_TIME = Period.ofDays(LifetimeActionTriggerType.MINIMUM_THRESHOLD_BEFORE_EXPIRY);
    @Autowired
    @Qualifier("keyBackupRestoreControllerV76")
    private com.github.nagyesta.lowkeyvault.controller.v7_6.KeyBackupRestoreController underTest;
    @Autowired
    private VaultService vaultService;

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(mock(VaultService.class), null))
                .add(Arguments.of(null, mock(KeyConverterRegistry.class)))
                .build();
    }

    @BeforeEach
    void setUp() {
        uri = getRandomVaultUri();
        vaultService.create(uri, RecoveryLevel.RECOVERABLE_AND_PURGEABLE, RecoveryLevel.MAX_RECOVERABLE_DAYS_INCLUSIVE, null);
    }

    @AfterEach
    void tearDown() {
        vaultService.delete(uri);
        vaultService.purge(uri);
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(final VaultService vaultService, final KeyConverterRegistry registry) {
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
    void testRestoreEntityShouldRestoreRotationPolicyWhenCalledWithValidInput() {
        //given
        final var backupModel = new KeyBackupModel();
        backupModel.setValue(new KeyBackupList());
        final var expectedKey = addVersionToList(uri, KEY_NAME_1, KEY_VERSION_1, backupModel, TAGS_EMPTY);
        final var keyEntityId = new KeyEntityId(uri, KEY_NAME_1);
        backupModel.getValue().setKeyRotationPolicy(keyRotationPolicy(keyEntityId));

        //when
        final var actual = underTest.restore(uri, backupModel);

        //then
        Assertions.assertNotNull(actual);
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertRestoredKeyMatchesExpectations(actualBody, (ECPublicKey) expectedKey.getPublic(), KEY_VERSION_1, TAGS_EMPTY);
        final var rotationPolicy = vaultService.findByUri(uri).keyVaultFake().rotationPolicy(keyEntityId);
        Assertions.assertEquals(keyEntityId, rotationPolicy.getId());
        Assertions.assertEquals(TIME_10_MINUTES_AGO, rotationPolicy.getCreatedOn());
        Assertions.assertEquals(NOW, rotationPolicy.getUpdatedOn());
        Assertions.assertEquals(EXPIRY_TIME, rotationPolicy.getExpiryTime());
        Assertions.assertIterableEquals(Collections.singleton(ROTATE), rotationPolicy.getLifetimeActions().keySet());
        final var lifetimeAction = rotationPolicy.getLifetimeActions().get(ROTATE);
        Assertions.assertEquals(ROTATE, lifetimeAction.actionType());
        Assertions.assertEquals(LifetimeActionTriggerType.TIME_AFTER_CREATE, lifetimeAction.trigger().triggerType());
        Assertions.assertEquals(TRIGGER_TIME, lifetimeAction.trigger().timePeriod());
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

    private KeyRotationPolicyModel keyRotationPolicy(final KeyEntityId keyEntityId) {
        final var model = new KeyRotationPolicyModel();
        model.setId(keyEntityId.asRotationPolicyUri(keyEntityId.vault()));
        model.setLifetimeActions(List.of(actionModel()));
        model.setAttributes(rotationPolicyAttributes());
        return model;
    }

    private KeyRotationPolicyAttributes rotationPolicyAttributes() {
        final var attributes = new KeyRotationPolicyAttributes();
        attributes.setCreated(TIME_10_MINUTES_AGO);
        attributes.setUpdated(NOW);
        attributes.setExpiryTime(EXPIRY_TIME);
        return attributes;
    }

    private KeyLifetimeActionModel actionModel() {
        final var actionModel = new KeyLifetimeActionModel();
        actionModel.setAction(new KeyLifetimeActionTypeModel(ROTATE));
        actionModel.setTrigger(new KeyLifetimeActionTriggerModel(null, TRIGGER_TIME));
        return actionModel;
    }

}
