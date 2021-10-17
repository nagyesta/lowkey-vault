package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.CreateSecretRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.UpdateSecretRequest;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultStub;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class SecretControllerTest {

    private static final KeyVaultSecretModel RESPONSE = createResponse();
    private static final DeletedKeyVaultSecretModel DELETED_RESPONSE = createDeletedResponse();
    @Mock
    private SecretEntityToV72ModelConverter secretEntityToV72ModelConverter;
    @Mock
    private SecretEntityToV72SecretItemModelConverter secretEntityToV72SecretItemModelConverter;
    @Mock
    private SecretEntityToV72SecretVersionItemModelConverter secretEntityToV72SecretVersionItemModelConverter;
    @Mock
    private VaultService vaultService;
    @Mock
    private VaultStub vaultStub;
    @Mock
    private SecretVaultStub secretVaultStub;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> entities;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<SecretEntityId, VersionedSecretEntityId, ReadOnlyKeyVaultSecretEntity> deletedEntities;
    private SecretController underTest;
    private AutoCloseable openMocks;

    private static KeyVaultSecretModel createResponse() {
        final KeyVaultSecretModel model = new KeyVaultSecretModel();
        model.setValue(LOWKEY_VAULT);
        model.setAttributes(new SecretPropertiesModel());
        model.setTags(Map.of());
        return model;
    }

    private static DeletedKeyVaultSecretModel createDeletedResponse() {
        final DeletedKeyVaultSecretModel model = new DeletedKeyVaultSecretModel();
        model.setValue(LOWKEY_VAULT);
        model.setAttributes(new SecretPropertiesModel());
        model.setTags(Map.of());
        model.setDeletedDate(TIME_10_MINUTES_AGO);
        model.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        model.setRecoveryId(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1.asRecoveryUri().toString());
        return model;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> secretAttributeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(null, null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 90, null, null))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 90, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 42, null, null))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 42, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, null, null, null))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .build();
    }

    public static Stream<Arguments> nullProvider() {
        final SecretEntityToV72ModelConverter ec = mock(SecretEntityToV72ModelConverter.class);
        final SecretEntityToV72SecretItemModelConverter ic = mock(SecretEntityToV72SecretItemModelConverter.class);
        final SecretEntityToV72SecretVersionItemModelConverter vic = mock(SecretEntityToV72SecretVersionItemModelConverter.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(ec, null, null, null))
                .add(Arguments.of(null, ic, null, null))
                .add(Arguments.of(null, null, vic, null))
                .add(Arguments.of(null, null, null, mock(VaultService.class)))
                .add(Arguments.of(null, ic, vic, mock(VaultService.class)))
                .add(Arguments.of(ec, null, vic, mock(VaultService.class)))
                .add(Arguments.of(ec, ic, null, mock(VaultService.class)))
                .add(Arguments.of(ec, ic, vic, null))
                .build();
    }

    public static Stream<Arguments> updateAttributeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES, null, TAGS_EMPTY))
                .add(Arguments.of(null, TIME_IN_10_MINUTES, null, null))
                .add(Arguments.of(null, null, true, TAGS_THREE_KEYS))
                .add(Arguments.of(TIME_10_MINUTES_AGO, null, false, TAGS_TWO_KEYS))
                .add(Arguments.of(TIME_IN_10_MINUTES, null, null, TAGS_TWO_KEYS))
                .add(Arguments.of(TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES, false, TAGS_TWO_KEYS))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        underTest = new SecretController(secretEntityToV72ModelConverter, secretEntityToV72SecretItemModelConverter,
                secretEntityToV72SecretVersionItemModelConverter, vaultService);
        when(vaultService.findByUri(eq(HTTPS_LOCALHOST_8443))).thenReturn(vaultStub);
        when(vaultStub.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        when(vaultStub.secretVaultStub()).thenReturn(secretVaultStub);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final SecretEntityToV72ModelConverter secretEntityToV72ModelConverter,
            final SecretEntityToV72SecretItemModelConverter secretEntityToV72SecretItemModelConverter,
            final SecretEntityToV72SecretVersionItemModelConverter secretEntityToV72SecretVersionItemModelConverter,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecretController(secretEntityToV72ModelConverter, secretEntityToV72SecretItemModelConverter,
                        secretEntityToV72SecretVersionItemModelConverter, vaultService));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testCreateShouldUseInputParametersWhenCalled(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        when(vaultStub.getRecoveryLevel()).thenReturn(RecoveryLevel.PURGEABLE);
        when(vaultStub.getRecoverableDays()).thenReturn(null);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(secretVaultStub.createSecretVersion(eq(SECRET_NAME_1), eq(request.getValue()), eq(request.getContentType())))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.create(SECRET_NAME_1, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(secretVaultStub).createSecretVersion(eq(SECRET_NAME_1), eq(request.getValue()), eq(request.getContentType()));
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub).setExpiry(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1), eq(notBefore), eq(expiry));
        verify(secretVaultStub).setEnabled(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1), eq(true));
        verify(secretVaultStub).addTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1), same(TAGS_TWO_KEYS));
        verify(secretEntityToV72ModelConverter).convert(same(entity));
    }

    @Test
    void testVersionsShouldThrowExceptionWhenSecretIsNotFound() {
        //given
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null))))
                .thenThrow(new NotFoundException("not found"));

        //when
        Assertions.assertThrows(NotFoundException.class,
                () -> underTest.versions(SECRET_NAME_1, HTTPS_LOCALHOST_8443, 0, 0));

        //then + exception
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testVersionsShouldFilterTheListReturnedWhenSecretIsFoundAndHasMoreVersionsThanNeeded() {
        //given
        final int index = 30;
        final LinkedList<String> fullList = IntStream.range(0, 42)
                .mapToObj(i -> UUID.randomUUID().toString().replaceAll("-", ""))
                .collect(Collectors.toCollection(LinkedList::new));
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        final String expectedNextUri = baseUri.asUri("versions?api-version=7.2&$skiptoken=31&maxresults=1").toString();
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(baseUri))).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final VersionedSecretEntityId secretEntityId = invocation.getArgument(0, VersionedSecretEntityId.class);
            return createEntity(secretEntityId, createRequest(null, null));
        });
        when(secretEntityToV72SecretVersionItemModelConverter.convert(any())).thenAnswer(invocation -> {
            final KeyVaultSecretEntity entity = invocation.getArgument(0, KeyVaultSecretEntity.class);
            return keyVaultSecretItemModel(entity.getId().asUri(), Map.of());
        });
        final URI expected = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, fullList.get(index)).asUri();

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> actual =
                underTest.versions(SECRET_NAME_1, HTTPS_LOCALHOST_8443, 1, index);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultItemListModel<KeyVaultSecretItemModel> actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals(expected.toString(), actualBody.getValue().get(0).getId());
        Assertions.assertNotNull(actualBody.getNextLink());
        Assertions.assertEquals(expectedNextUri, actualBody.getNextLink());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testVersionsShouldNotContainNextUriWhenLastPageIsReturnedFully() {
        //given
        final LinkedList<String> fullList = IntStream.range(0, 25)
                .mapToObj(i -> UUID.randomUUID().toString().replaceAll("-", ""))
                .collect(Collectors.toCollection(LinkedList::new));
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(baseUri))).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final VersionedSecretEntityId secretEntityId = invocation.getArgument(0, VersionedSecretEntityId.class);
            return createEntity(secretEntityId, createRequest(null, null));
        });
        when(secretEntityToV72SecretVersionItemModelConverter.convert(any())).thenAnswer(invocation -> {
            final KeyVaultSecretEntity entity = invocation.getArgument(0, KeyVaultSecretEntity.class);
            return keyVaultSecretItemModel(entity.getId().asUri(), Map.of());
        });
        final List<URI> expected = fullList.stream()
                .map(e -> new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, e).asUri())
                .collect(Collectors.toList());

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> actual =
                underTest.versions(SECRET_NAME_1, HTTPS_LOCALHOST_8443, 25, 0);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultItemListModel<KeyVaultSecretItemModel> actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        final List<URI> actualList = actualBody.getValue().stream()
                .map(KeyVaultSecretItemModel::getId)
                .map(URI::create)
                .collect(Collectors.toList());
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertIterableEquals(expected, actualList);
        Assertions.assertNull(actualBody.getNextLink());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testDeleteSecretShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(secretVaultStub.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(secretVaultStub).delete(eq(baseUri));
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(deletedEntities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(deletedEntities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convertDeleted(same(entity)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.delete(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        final InOrder inOrder = inOrder(secretVaultStub);
        inOrder.verify(secretVaultStub).delete(eq(baseUri));
        inOrder.verify(secretVaultStub, atLeastOnce()).getDeletedEntities();
        verify(secretVaultStub, never()).getEntities();
        verify(deletedEntities).getLatestVersionOfEntity(eq(baseUri));
        verify(deletedEntities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testRecoverDeletedSecretShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(secretVaultStub.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(secretVaultStub).delete(eq(baseUri));
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.recoverDeletedSecret(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        final InOrder inOrder = inOrder(secretVaultStub);
        inOrder.verify(secretVaultStub).recover(eq(baseUri));
        inOrder.verify(secretVaultStub, atLeastOnce()).getEntities();
        verify(secretVaultStub, never()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetDeletedSecretShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getDeletedEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convertDeleted(same(entity)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.getDeletedSecret(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub, never()).getEntities();
        verify(secretVaultStub, atLeastOnce()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.get(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub, atLeastOnce()).getEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetSecretsShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final KeyVaultSecretItemModel secretItemModel = keyVaultSecretItemModel(baseUri.asUri(), Map.of());
        when(secretEntityToV72SecretItemModelConverter.convert(same(entity)))
                .thenReturn(secretItemModel);

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> actual =
                underTest.listSecrets(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(secretItemModel, actual.getBody().getValue().get(0));
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub, atLeastOnce()).getEntities();
        verify(secretVaultStub, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convert(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetSecretsShouldReturnNextLinkWhenNotOnLastPage(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final KeyVaultSecretItemModel secretItemModel = keyVaultSecretItemModel(baseUri.asUri(), Map.of());
        when(secretEntityToV72SecretItemModelConverter.convert(same(entity)))
                .thenReturn(secretItemModel);

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> actual =
                underTest.listSecrets(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(secretItemModel, actual.getBody().getValue().get(0));
        final String expectedNextLink = HTTPS_LOCALHOST_8443 + "/secrets?api-version=7.2&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub, atLeastOnce()).getEntities();
        verify(secretVaultStub, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convert(same(entity));
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetDeletedSecretsShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getDeletedEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final DeletedKeyVaultSecretItemModel secretItemModel = deletedKeyVaultSecretItemModel(baseUri, Map.of());
        when(secretEntityToV72SecretItemModelConverter.convertDeleted(same(entity)))
                .thenReturn(secretItemModel);

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> actual =
                underTest.listDeletedSecrets(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(secretItemModel, actual.getBody().getValue().get(0));
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub, atLeastOnce()).getDeletedEntities();
        verify(secretVaultStub, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetDeletedSecretsShouldReturnNextLinkWhenNotOnLastPage(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultStub.getDeletedEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final DeletedKeyVaultSecretItemModel secretItemModel = deletedKeyVaultSecretItemModel(baseUri, Map.of());
        when(secretEntityToV72SecretItemModelConverter.convertDeleted(same(entity)))
                .thenReturn(secretItemModel);

        //when
        final ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> actual =
                underTest.listDeletedSecrets(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(secretItemModel, actual.getBody().getValue().get(0));
        final String expectedNextLink = HTTPS_LOCALHOST_8443 + "/deletedsecrets?api-version=7.2&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub, atLeastOnce()).getDeletedEntities();
        verify(secretVaultStub, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetWithVersionShouldReturnEntryWhenSecretAndVersionIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.getWithVersion(SECRET_NAME_1, SECRET_VERSION_3, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("updateAttributeProvider")
    void testUpdateVersionShouldReturnEntryWhenSecretAndVersionIsFound(
            final OffsetDateTime expiry, final OffsetDateTime notBefore,
            final Boolean enabled, final Map<String, String> tags) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        final CreateSecretRequest createSecretRequest = createRequest(null, null);
        final UpdateSecretRequest updateSecretRequest = new UpdateSecretRequest();
        if (tags != null) {
            updateSecretRequest.setTags(tags);
        }
        if (enabled != null || expiry != null || notBefore != null) {
            final BasePropertiesUpdateModel properties = new BasePropertiesUpdateModel();
            properties.setEnabled(enabled);
            properties.setExpiresOn(expiry);
            properties.setNotBefore(notBefore);
            updateSecretRequest.setProperties(properties);
        }
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, createSecretRequest);
        when(secretVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(RecoveryLevel.PURGEABLE);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest
                .updateVersion(SECRET_NAME_1, SECRET_VERSION_3, HTTPS_LOCALHOST_8443, updateSecretRequest);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).secretVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(secretVaultStub).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        final InOrder inOrder = inOrder(secretVaultStub, entities);
        if (enabled != null) {
            inOrder.verify(secretVaultStub)
                    .setEnabled(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), eq(enabled));
        } else {
            inOrder.verify(secretVaultStub, never())
                    .setEnabled(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), anyBoolean());
        }
        if (expiry != null || notBefore != null) {
            inOrder.verify(secretVaultStub)
                    .setExpiry(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), eq(notBefore), eq(expiry));
        } else {
            inOrder.verify(secretVaultStub, never())
                    .setExpiry(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), any(), any());
        }
        if (tags != null) {
            inOrder.verify(secretVaultStub)
                    .clearTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
            inOrder.verify(secretVaultStub)
                    .addTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), same(updateSecretRequest.getTags()));
        } else {
            inOrder.verify(secretVaultStub, never())
                    .clearTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
            inOrder.verify(secretVaultStub, never())
                    .addTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), anyMap());
        }
        inOrder.verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity));
    }

    @NonNull
    private CreateSecretRequest createRequest(
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        final CreateSecretRequest secretRequest = new CreateSecretRequest();
        secretRequest.setValue(LOWKEY_VAULT);
        final SecretPropertiesModel properties = new SecretPropertiesModel();
        properties.setExpiresOn(expiry);
        properties.setNotBefore(notBefore);
        properties.setEnabled(true);
        secretRequest.setProperties(properties);
        secretRequest.setTags(TAGS_TWO_KEYS);
        return secretRequest;
    }

    @NonNull
    private KeyVaultSecretEntity createEntity(final VersionedSecretEntityId secretEntityId, final CreateSecretRequest createSecretRequest) {
        return new KeyVaultSecretEntity(secretEntityId, vaultStub, createSecretRequest.getValue(), createSecretRequest.getContentType());
    }

    private KeyVaultSecretItemModel keyVaultSecretItemModel(final URI asUriNoVersion, final Map<String, String> tags) {
        final KeyVaultSecretItemModel model = new KeyVaultSecretItemModel();
        model.setAttributes(new SecretPropertiesModel());
        model.setId(asUriNoVersion.toString());
        model.setTags(tags);
        return model;
    }

    private DeletedKeyVaultSecretItemModel deletedKeyVaultSecretItemModel(final SecretEntityId id, final Map<String, String> tags) {
        final DeletedKeyVaultSecretItemModel model = new DeletedKeyVaultSecretItemModel();
        model.setAttributes(new SecretPropertiesModel());
        model.setId(id.asUriNoVersion().toString());
        model.setTags(tags);
        model.setDeletedDate(TIME_10_MINUTES_AGO);
        model.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        model.setRecoveryId(id.asRecoveryUri().toString());
        return model;
    }
}
