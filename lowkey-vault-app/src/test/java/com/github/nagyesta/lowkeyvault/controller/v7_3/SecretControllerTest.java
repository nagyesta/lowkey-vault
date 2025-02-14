package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.CreateSecretRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.UpdateSecretRequest;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.impl.SecretCreateInput;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsSecrets.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
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
    private VaultFake vaultFake;
    @Mock
    private SecretVaultFake secretVaultFake;
    @Mock
    private SecretConverterRegistry registry;
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
        model.setRecoveryId(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1.asRecoveryUri(HTTPS_LOCALHOST_8443).toString());
        return model;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> secretAttributeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null))
                .add(Arguments.of(null, null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 90, null, null))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE, 90, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 90, null, null))
                .add(Arguments.of(RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 90, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 42, null, null))
                .add(Arguments.of(RecoveryLevel.CUSTOMIZED_RECOVERABLE, 42, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, null, null, null))
                .add(Arguments.of(RecoveryLevel.PURGEABLE, null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .build();
    }

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(mock(SecretConverterRegistry.class), null))
                .add(Arguments.of(null, mock(VaultService.class)))
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
        when(registry.modelConverter(eq(ApiConstants.V_7_3))).thenReturn(secretEntityToV72ModelConverter);
        when(registry.itemConverter(eq(ApiConstants.V_7_3))).thenReturn(secretEntityToV72SecretItemModelConverter);
        when(registry.versionedItemConverter(eq(ApiConstants.V_7_3)))
                .thenReturn(secretEntityToV72SecretVersionItemModelConverter);
        when(registry.versionedEntityId(any(URI.class), anyString(), anyString())).thenCallRealMethod();
        when(registry.entityId(any(URI.class), anyString())).thenCallRealMethod();
        underTest = new SecretController(registry, vaultService);
        when(vaultService.findByUri(eq(HTTPS_LOCALHOST_8443))).thenReturn(vaultFake);
        when(vaultFake.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        when(vaultFake.secretVaultFake()).thenReturn(secretVaultFake);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final SecretConverterRegistry registry,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new SecretController(registry, vaultService));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testCreateShouldUseInputParametersWhenCalled(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        when(vaultFake.getRecoveryLevel()).thenReturn(RecoveryLevel.PURGEABLE);
        when(vaultFake.getRecoverableDays()).thenReturn(null);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        final ArgumentCaptor<SecretCreateInput> input = ArgumentCaptor.forClass(SecretCreateInput.class);
        when(secretVaultFake.createSecretVersion(eq(SECRET_NAME_1), input.capture()))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1);
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.create(SECRET_NAME_1, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(secretVaultFake).createSecretVersion(eq(SECRET_NAME_1), any(SecretCreateInput.class));
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        final SecretCreateInput captured = input.getValue();
        Assertions.assertEquals(request.getValue(), captured.getValue());
        Assertions.assertEquals(request.getContentType(), captured.getContentType());
        Assertions.assertEquals(request.getProperties().getExpiresOn(), captured.getExpiresOn());
        Assertions.assertEquals(request.getProperties().getNotBefore(), captured.getNotBefore());
    }

    @Test
    void testVersionsShouldThrowExceptionWhenSecretIsNotFound() {
        //given
        when(secretVaultFake.getEntities())
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
                .sorted()
                .collect(Collectors.toCollection(LinkedList::new));
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        final String expectedNextUri = baseUri.asUri(HTTPS_LOCALHOST_8443, "versions?api-version=7.3&$skiptoken=31&maxresults=1")
                .toString();
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(baseUri))).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final VersionedSecretEntityId secretEntityId = invocation.getArgument(0, VersionedSecretEntityId.class);
            return createEntity(secretEntityId, createRequest(null, null));
        });
        when(secretEntityToV72SecretVersionItemModelConverter.convert(any(), any())).thenAnswer(invocation -> {
            final KeyVaultSecretEntity entity = invocation.getArgument(0, KeyVaultSecretEntity.class);
            return keyVaultSecretItemModel(entity.getId().asUri(HTTPS_LOCALHOST_8443), Map.of());
        });
        final URI expected = new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1,
                fullList.get(index)).asUri(HTTPS_LOCALHOST_8443);

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
                .sorted()
                .collect(Collectors.toCollection(LinkedList::new));
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(baseUri))).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final VersionedSecretEntityId secretEntityId = invocation.getArgument(0, VersionedSecretEntityId.class);
            return createEntity(secretEntityId, createRequest(null, null));
        });
        when(secretEntityToV72SecretVersionItemModelConverter.convert(any(), any())).thenAnswer(invocation -> {
            final KeyVaultSecretEntity entity = invocation.getArgument(0, KeyVaultSecretEntity.class);
            return keyVaultSecretItemModel(entity.getId().asUri(HTTPS_LOCALHOST_8443), Map.of());
        });
        final List<URI> expected = fullList.stream()
                .map(e -> new VersionedSecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, e).asUri(HTTPS_LOCALHOST_8443))
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
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(secretVaultFake.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(secretVaultFake).delete(eq(baseUri));
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(deletedEntities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(deletedEntities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final ResponseEntity<DeletedKeyVaultSecretModel> actual = underTest.delete(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        final InOrder inOrder = inOrder(secretVaultFake);
        inOrder.verify(secretVaultFake).delete(eq(baseUri));
        inOrder.verify(secretVaultFake, atLeastOnce()).getDeletedEntities();
        verify(secretVaultFake, never()).getEntities();
        verify(deletedEntities).getLatestVersionOfEntity(eq(baseUri));
        verify(deletedEntities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testRecoverDeletedSecretShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(secretVaultFake.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(secretVaultFake).delete(eq(baseUri));
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.recoverDeletedSecret(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        final InOrder inOrder = inOrder(secretVaultFake);
        inOrder.verify(secretVaultFake).recover(eq(baseUri));
        inOrder.verify(secretVaultFake, atLeastOnce()).getEntities();
        verify(secretVaultFake, never()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetDeletedSecretShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.getDeletedSecret(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, never()).getEntities();
        verify(secretVaultFake, atLeastOnce()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetDeletedSecretShouldThrowExceptionWhenSecretIsDisabled(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setEnabled(false);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(DELETED_RESPONSE);

        //when
        Assertions.assertThrows(NotFoundException.class, () -> underTest.getDeletedSecret(SECRET_NAME_1, HTTPS_LOCALHOST_8443));

        //then
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, never()).getEntities();
        verify(secretVaultFake, atLeastOnce()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter, never()).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testPurgeDeletedShouldSucceedWhenDeletedSecretIsPurgeable(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        final RecoveryLevel nonNullRecoveryLevel = Optional.ofNullable(recoveryLevel).orElse(RecoveryLevel.PURGEABLE);
        if (!nonNullRecoveryLevel.isPurgeable()) {
            doThrow(IllegalStateException.class).when(secretVaultFake).purge(eq(UNVERSIONED_SECRET_ENTITY_ID_1));
        }

        //when
        if (nonNullRecoveryLevel.isPurgeable()) {
            final ResponseEntity<Void> response = underTest.purgeDeleted(SECRET_NAME_1, HTTPS_LOCALHOST_8443);
            Assertions.assertNotNull(response);
            Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        } else {
            Assertions.assertThrows(IllegalStateException.class, () -> underTest.purgeDeleted(SECRET_NAME_1, HTTPS_LOCALHOST_8443));
        }

        //then
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, never()).getEntities();
        verify(secretVaultFake, never()).getDeletedEntities();
        verify(secretVaultFake, atLeastOnce()).purge(eq(UNVERSIONED_SECRET_ENTITY_ID_1));
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities, never()).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter, never()).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.get(SECRET_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, atLeastOnce()).getEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetSecretsShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final KeyVaultSecretItemModel secretItemModel = keyVaultSecretItemModel(baseUri.asUri(HTTPS_LOCALHOST_8443), Map.of());
        when(secretEntityToV72SecretItemModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
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
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, atLeastOnce()).getEntities();
        verify(secretVaultFake, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetSecretsShouldReturnNextLinkWhenNotOnLastPage(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final KeyVaultSecretItemModel secretItemModel = keyVaultSecretItemModel(baseUri.asUri(HTTPS_LOCALHOST_8443), Map.of());
        when(secretEntityToV72SecretItemModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
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
        final String expectedNextLink = HTTPS_LOCALHOST_8443 + "/secrets?api-version=7.3&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, atLeastOnce()).getEntities();
        verify(secretVaultFake, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetDeletedSecretsShouldReturnEntryWhenSecretIsFound(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final DeletedKeyVaultSecretItemModel secretItemModel = deletedKeyVaultSecretItemModel(baseUri, Map.of());
        when(secretEntityToV72SecretItemModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(secretItemModel);

        //when
        final ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultSecretItemModel>> actual =
                underTest.listDeletedSecrets(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(secretItemModel, actual.getBody().getValue().get(0));
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, atLeastOnce()).getDeletedEntities();
        verify(secretVaultFake, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetDeletedSecretsShouldReturnNextLinkWhenNotOnLastPage(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        when(secretVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final DeletedKeyVaultSecretItemModel secretItemModel = deletedKeyVaultSecretItemModel(baseUri, Map.of());
        when(secretEntityToV72SecretItemModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(secretItemModel);

        //when
        final ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultSecretItemModel>> actual =
                underTest.listDeletedSecrets(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(secretItemModel, actual.getBody().getValue().get(0));
        final String expectedNextLink = HTTPS_LOCALHOST_8443 + "/deletedsecrets?api-version=7.3&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake, atLeastOnce()).getDeletedEntities();
        verify(secretVaultFake, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(secretEntityToV72SecretItemModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
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
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest.getWithVersion(SECRET_NAME_1, SECRET_VERSION_3, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("secretAttributeProvider")
    void testGetWithVersionShouldThrowExceptionWhenSecretAndVersionIsFoundButSecretVersionIsDisabled(
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final SecretEntityId baseUri = new SecretEntityId(HTTPS_LOCALHOST_8443, SECRET_NAME_1, null);
        final CreateSecretRequest request = createRequest(expiry, notBefore);
        final ReadOnlyKeyVaultSecretEntity entity = createEntity(VERSIONED_SECRET_ENTITY_ID_1_VERSION_1, request);
        entity.setEnabled(false);
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        Assertions.assertThrows(NotFoundException.class, () ->
                underTest.getWithVersion(SECRET_NAME_1, SECRET_VERSION_3, HTTPS_LOCALHOST_8443));

        //then
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter, never()).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
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
        when(secretVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(RecoveryLevel.PURGEABLE);
        when(entities.getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(secretEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultSecretModel> actual = underTest
                .updateVersion(SECRET_NAME_1, SECRET_VERSION_3, HTTPS_LOCALHOST_8443, updateSecretRequest);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake).secretVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(secretVaultFake).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        final InOrder inOrder = inOrder(secretVaultFake, entities);
        if (enabled != null) {
            inOrder.verify(secretVaultFake)
                    .setEnabled(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), eq(enabled));
        } else {
            inOrder.verify(secretVaultFake, never())
                    .setEnabled(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), anyBoolean());
        }
        if (expiry != null || notBefore != null) {
            inOrder.verify(secretVaultFake)
                    .setExpiry(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), eq(notBefore), eq(expiry));
        } else {
            inOrder.verify(secretVaultFake, never())
                    .setExpiry(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), any(), any());
        }
        if (tags != null) {
            inOrder.verify(secretVaultFake)
                    .clearTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
            inOrder.verify(secretVaultFake)
                    .addTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), same(updateSecretRequest.getTags()));
        } else {
            inOrder.verify(secretVaultFake, never())
                    .clearTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
            inOrder.verify(secretVaultFake, never())
                    .addTags(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3), anyMap());
        }
        inOrder.verify(entities).getReadOnlyEntity(eq(VERSIONED_SECRET_ENTITY_ID_1_VERSION_3));
        verify(secretEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
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
        return new KeyVaultSecretEntity(secretEntityId, vaultFake, createSecretRequest.getValue(), createSecretRequest.getContentType());
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
        model.setId(id.asUriNoVersion(id.vault()).toString());
        model.setTags(tags);
        model.setDeletedDate(TIME_10_MINUTES_AGO);
        model.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        model.setRecoveryId(id.asRecoveryUri(id.vault()).toString());
        return model;
    }
}
