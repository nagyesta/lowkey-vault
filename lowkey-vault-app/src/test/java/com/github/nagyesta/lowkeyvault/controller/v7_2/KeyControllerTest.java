package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.service.vault.VaultStub;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class KeyControllerTest {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final KeyVaultKeyModel RESPONSE = createResponse();
    private static final DeletedKeyVaultKeyModel DELETED_RESPONSE = createDeletedResponse();
    @Mock
    private KeyEntityToV72ModelConverter keyEntityToV72ModelConverter;
    @Mock
    private KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter;
    @Mock
    private KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter;
    @Mock
    private VaultService vaultService;
    @Mock
    private VaultStub vaultStub;
    @Mock
    private KeyVaultStub keyVaultStub;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> entities;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> deletedEntities;
    private KeyController underTest;
    private AutoCloseable openMocks;

    private static KeyVaultKeyModel createResponse() {
        final KeyVaultKeyModel model = new KeyVaultKeyModel();
        model.setKey(new JsonWebKeyModel());
        model.setAttributes(new KeyPropertiesModel());
        model.setTags(Map.of());
        return model;
    }

    private static DeletedKeyVaultKeyModel createDeletedResponse() {
        final DeletedKeyVaultKeyModel model = new DeletedKeyVaultKeyModel();
        model.setKey(new JsonWebKeyModel());
        model.setAttributes(new KeyPropertiesModel());
        model.setTags(Map.of());
        model.setDeletedDate(TIME_10_MINUTES_AGO);
        model.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        model.setRecoveryId(VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asRecoveryUri().toString());
        return model;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    public static Stream<Arguments> keyAttributeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null,
                        null, null, null, null))
                .add(Arguments.of(List.of(),
                        null, null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(List.of(),
                        RecoveryLevel.RECOVERABLE, 90, null, null))
                .add(Arguments.of(List.of(),
                        RecoveryLevel.RECOVERABLE, 90, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(List.of(KeyOperation.ENCRYPT),
                        RecoveryLevel.CUSTOMIZED_RECOVERABLE, 42, null, null))
                .add(Arguments.of(List.of(KeyOperation.ENCRYPT),
                        RecoveryLevel.CUSTOMIZED_RECOVERABLE, 42, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .add(Arguments.of(Arrays.asList(KeyOperation.values()),
                        RecoveryLevel.PURGEABLE, null, null, null))
                .add(Arguments.of(Arrays.asList(KeyOperation.values()),
                        RecoveryLevel.PURGEABLE, null, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
                .build();
    }

    public static Stream<Arguments> nullProvider() {
        final KeyEntityToV72ModelConverter ec = mock(KeyEntityToV72ModelConverter.class);
        final KeyEntityToV72KeyItemModelConverter ic = mock(KeyEntityToV72KeyItemModelConverter.class);
        final KeyEntityToV72KeyVersionItemModelConverter vic = mock(KeyEntityToV72KeyVersionItemModelConverter.class);
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

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        underTest = new KeyController(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService);
        when(vaultService.findByUri(eq(HTTPS_LOCALHOST_8443))).thenReturn(vaultStub);
        when(vaultStub.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        when(vaultStub.keyVaultStub()).thenReturn(keyVaultStub);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
            final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
            final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyController(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                        keyEntityToV72KeyVersionItemModelConverter, vaultService));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testCreateShouldUseInputParametersWhenCalled(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        when(vaultStub.getRecoveryLevel()).thenReturn(RecoveryLevel.PURGEABLE);
        when(vaultStub.getRecoverableDays()).thenReturn(null);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(keyVaultStub.createKeyVersion(eq(KEY_NAME_1), eq(request.toKeyCreationInput())))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_1);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.create(KEY_NAME_1, HTTPS_LOCALHOST_8443, request);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(keyVaultStub).createKeyVersion(eq(KEY_NAME_1), eq(request.toKeyCreationInput()));
        verify(keyVaultStub)
                .setKeyOperations(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), eq(operations));
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub).setExpiry(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), eq(notBefore), eq(expiry));
        verify(keyVaultStub).setEnabled(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), eq(true));
        verify(keyVaultStub).addTags(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), same(TAGS_TWO_KEYS));
        verify(keyEntityToV72ModelConverter).convert(same(entity));
    }

    @Test
    void testVersionsShouldThrowExceptionWhenKeyIsNotFound() {
        //given
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null))))
                .thenThrow(new NotFoundException("not found"));

        //when
        Assertions.assertThrows(NotFoundException.class,
                () -> underTest.versions(KEY_NAME_1, HTTPS_LOCALHOST_8443, 0, 0));

        //then + exception
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testVersionsShouldFilterTheListReturnedWhenKeyIsFoundAndHasMoreVersionsThanNeeded() {
        //given
        final int index = 30;
        final LinkedList<String> fullList = IntStream.range(0, 42)
                .mapToObj(i -> UUID.randomUUID().toString().replaceAll("-", ""))
                .collect(Collectors.toCollection(LinkedList::new));
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final String expectedNextUri = baseUri.asUri("versions?api-version=7.2&$skiptoken=31&maxresults=1").toString();
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(baseUri))).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final VersionedKeyEntityId keyEntityId = invocation.getArgument(0, VersionedKeyEntityId.class);
            return createEntity(keyEntityId, createRequest(null, null, null));
        });
        when(keyEntityToV72KeyVersionItemModelConverter.convert(any())).thenAnswer(invocation -> {
            final KeyVaultKeyEntity<?, ?> entity = invocation.getArgument(0, KeyVaultKeyEntity.class);
            return keyVaultKeyItemModel(entity.getId().asUri(), Map.of());
        });
        final URI expected = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, fullList.get(index)).asUri();

        //when
        final ResponseEntity<KeyVaultKeyItemListModel> actual = underTest.versions(KEY_NAME_1, HTTPS_LOCALHOST_8443, 1, index);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultKeyItemListModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals(expected.toString(), actualBody.getValue().get(0).getKeyId());
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
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(eq(baseUri))).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final VersionedKeyEntityId keyEntityId = invocation.getArgument(0, VersionedKeyEntityId.class);
            return createEntity(keyEntityId, createRequest(null, null, null));
        });
        when(keyEntityToV72KeyVersionItemModelConverter.convert(any())).thenAnswer(invocation -> {
            final KeyVaultKeyEntity<?, ?> entity = invocation.getArgument(0, KeyVaultKeyEntity.class);
            return keyVaultKeyItemModel(entity.getId().asUri(), Map.of());
        });
        final List<URI> expected = fullList.stream()
                .map(e -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, e).asUri())
                .collect(Collectors.toList());

        //when
        final ResponseEntity<KeyVaultKeyItemListModel> actual = underTest.versions(KEY_NAME_1, HTTPS_LOCALHOST_8443, 25, 0);

        //then
        Assertions.assertNotNull(actual);
        final KeyVaultKeyItemListModel actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        final List<URI> actualList = actualBody.getValue().stream()
                .map(KeyVaultKeyItemModel::getKeyId)
                .map(URI::create)
                .collect(Collectors.toList());
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertIterableEquals(expected, actualList);
        Assertions.assertNull(actualBody.getNextLink());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testDeleteKeyShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(keyVaultStub.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(keyVaultStub).delete(eq(baseUri));
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(deletedEntities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(deletedEntities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convertDeleted(same(entity)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.delete(KEY_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        final InOrder inOrder = inOrder(keyVaultStub);
        inOrder.verify(keyVaultStub).delete(eq(baseUri));
        inOrder.verify(keyVaultStub, atLeastOnce()).getDeletedEntities();
        verify(keyVaultStub, never()).getEntities();
        verify(deletedEntities).getLatestVersionOfEntity(eq(baseUri));
        verify(deletedEntities).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        verify(keyEntityToV72ModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testRecoverDeletedKeyShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(keyVaultStub.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(keyVaultStub).delete(eq(baseUri));
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.recoverDeletedKey(KEY_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        final InOrder inOrder = inOrder(keyVaultStub);
        inOrder.verify(keyVaultStub).recover(eq(baseUri));
        inOrder.verify(keyVaultStub, atLeastOnce()).getEntities();
        verify(keyVaultStub, never()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        verify(keyEntityToV72ModelConverter).convert(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetDeletedKeyShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getDeletedEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convertDeleted(same(entity)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.getDeletedKey(KEY_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub, never()).getEntities();
        verify(keyVaultStub, atLeastOnce()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        verify(keyEntityToV72ModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.get(KEY_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub, atLeastOnce()).getEntities();
        verify(entities).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        verify(keyEntityToV72ModelConverter).convert(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetKeysShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final KeyVaultKeyItemModel keyItemModel = keyVaultKeyItemModel(baseUri.asUri(), Map.of());
        when(keyEntityToV72KeyItemModelConverter.convert(same(entity)))
                .thenReturn(keyItemModel);

        //when
        final ResponseEntity<KeyVaultKeyItemListModel> actual = underTest.listKeys(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().get(0));
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub, atLeastOnce()).getEntities();
        verify(keyVaultStub, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convert(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetKeysShouldReturnNextLinkWhenNotOnLastPage(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final KeyVaultKeyItemModel keyItemModel = keyVaultKeyItemModel(baseUri.asUri(), Map.of());
        when(keyEntityToV72KeyItemModelConverter.convert(same(entity)))
                .thenReturn(keyItemModel);

        //when
        final ResponseEntity<KeyVaultKeyItemListModel> actual = underTest.listKeys(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().get(0));
        final String expectedNextLink = HTTPS_LOCALHOST_8443 + "/keys?api-version=7.2&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub, atLeastOnce()).getEntities();
        verify(keyVaultStub, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convert(same(entity));
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetDeletedKeysShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getDeletedEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final DeletedKeyVaultKeyItemModel keyItemModel = deletedKeyVaultKeyItemModel(baseUri, Map.of());
        when(keyEntityToV72KeyItemModelConverter.convertDeleted(same(entity)))
                .thenReturn(keyItemModel);

        //when
        final ResponseEntity<KeyVaultKeyItemListModel> actual = underTest.listDeletedKeys(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().get(0));
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub, atLeastOnce()).getDeletedEntities();
        verify(keyVaultStub, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetDeletedKeysShouldReturnNextLinkWhenNotOnLastPage(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getDeletedEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final DeletedKeyVaultKeyItemModel keyItemModel = deletedKeyVaultKeyItemModel(baseUri, Map.of());
        when(keyEntityToV72KeyItemModelConverter.convertDeleted(same(entity)))
                .thenReturn(keyItemModel);

        //when
        final ResponseEntity<KeyVaultKeyItemListModel> actual = underTest.listDeletedKeys(HTTPS_LOCALHOST_8443, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().get(0));
        final String expectedNextLink = HTTPS_LOCALHOST_8443 + "/deletedkeys?api-version=7.2&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub, atLeastOnce()).getDeletedEntities();
        verify(keyVaultStub, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convertDeleted(same(entity));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetWithVersionShouldReturnEntryWhenKeyAndVersionIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final CreateKeyRequest request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(vaultStub.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultStub.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);

        //when
        final ResponseEntity<KeyVaultKeyModel> actual = underTest.getWithVersion(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub).keyVaultStub();
        verify(vaultStub).getRecoveryLevel();
        verify(vaultStub).getRecoverableDays();
        verify(keyVaultStub).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        verify(keyEntityToV72ModelConverter).convert(same(entity));
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @ValueSource(strings = {BLANK, DEFAULT_VAULT, LOCALHOST, LOWKEY_VAULT})
    void testEncryptAndDecryptShouldGetBackOriginalInputWhenKeyAndVersionIsFound(final String clearText) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final List<KeyOperation> operations = List.of(
                KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY);
        final CreateKeyRequest request = createRequest(operations, null, null);
        final RsaKeyVaultKeyEntity entity = (RsaKeyVaultKeyEntity) createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setEnabled(true);
        entity.setOperations(operations);
        when(keyVaultStub.getEntities())
                .thenReturn(entities);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity)))
                .thenReturn(RESPONSE);
        final KeyOperationsParameters encryptParameters = new KeyOperationsParameters();
        encryptParameters.setAlgorithm(EncryptionAlgorithm.RSA_OAEP_256);
        encryptParameters.setValue(ENCODER.encodeToString(clearText.getBytes(StandardCharsets.UTF_8)));

        //when
        final ResponseEntity<KeyOperationsResult> encrypted = underTest
                .encrypt(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, encryptParameters);
        Assertions.assertNotNull(encrypted);
        Assertions.assertEquals(HttpStatus.OK, encrypted.getStatusCode());
        Assertions.assertNotNull(encrypted.getBody());
        Assertions.assertNotEquals(clearText, encrypted.getBody().getValue());

        final KeyOperationsParameters decryptParameters = new KeyOperationsParameters();
        decryptParameters.setAlgorithm(EncryptionAlgorithm.RSA_OAEP_256);
        decryptParameters.setValue(encrypted.getBody().getValue());
        final ResponseEntity<KeyOperationsResult> actual = underTest
                .decrypt(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, decryptParameters);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        final String decoded = new String(DECODER.decode(actual.getBody().getValue()), StandardCharsets.UTF_8);
        Assertions.assertEquals(clearText, decoded);

        verify(vaultService, times(2)).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultStub, times(2)).keyVaultStub();
        verify(keyVaultStub, times(2)).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities, times(2)).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
    }

    @NonNull
    private CreateKeyRequest createRequest(
            final List<KeyOperation> operations,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        final CreateKeyRequest keyRequest = new CreateKeyRequest();
        keyRequest.setKeyType(KeyType.RSA);
        keyRequest.setKeyOperations(operations);
        final KeyPropertiesModel properties = new KeyPropertiesModel();
        properties.setExpiresOn(expiry);
        properties.setNotBefore(notBefore);
        properties.setEnabled(true);
        keyRequest.setProperties(properties);
        keyRequest.setTags(TAGS_TWO_KEYS);
        return keyRequest;
    }

    @NonNull
    private KeyVaultKeyEntity<?, ?> createEntity(final VersionedKeyEntityId keyEntityId, final CreateKeyRequest createKeyRequest) {
        return new RsaKeyVaultKeyEntity(keyEntityId, vaultStub, createKeyRequest.getKeySize(), null, false);
    }

    private KeyVaultKeyItemModel keyVaultKeyItemModel(final URI asUriNoVersion, final Map<String, String> tags) {
        final KeyVaultKeyItemModel model = new KeyVaultKeyItemModel();
        model.setAttributes(new KeyPropertiesModel());
        model.setKeyId(asUriNoVersion.toString());
        model.setTags(tags);
        return model;
    }

    private DeletedKeyVaultKeyItemModel deletedKeyVaultKeyItemModel(final KeyEntityId id, final Map<String, String> tags) {
        final DeletedKeyVaultKeyItemModel model = new DeletedKeyVaultKeyItemModel();
        model.setAttributes(new KeyPropertiesModel());
        model.setKeyId(id.asUriNoVersion().toString());
        model.setTags(tags);
        model.setDeletedDate(TIME_10_MINUTES_AGO);
        model.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        model.setRecoveryId(id.asRecoveryUri().toString());
        return model;
    }
}
