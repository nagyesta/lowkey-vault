package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesUpdateModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.UpdateKeyRequest;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.exception.AlreadyExistsException;
import com.github.nagyesta.lowkeyvault.service.exception.CryptoException;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyCreateDetailedInput;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.assertj.core.util.Arrays;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.mockito.Mockito.*;

class KeyControllerTest {

    private static final KeyVaultKeyModel RESPONSE = createResponse();
    private static final DeletedKeyVaultKeyModel DELETED_RESPONSE = createDeletedResponse();
    @Mock
    private VaultService vaultService;
    @Mock
    private KeyEntityToV72ModelConverter keyEntityToV72ModelConverter;
    @Mock
    private KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter;
    @Mock
    private VaultFake vaultFake;
    @Mock
    private KeyVaultFake keyVaultFake;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> entities;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> deletedEntities;
    @InjectMocks
    private KeyController underTest;
    private AutoCloseable openMocks;

    private static KeyVaultKeyModel createResponse() {
        final var model = new KeyVaultKeyModel();
        model.setKey(new JsonWebKeyModel());
        model.setAttributes(new KeyPropertiesModel());
        model.setTags(Map.of());
        return model;
    }

    private static DeletedKeyVaultKeyModel createDeletedResponse() {
        final var model = new DeletedKeyVaultKeyModel();
        model.setKey(new JsonWebKeyModel());
        model.setAttributes(new KeyPropertiesModel());
        model.setTags(Map.of());
        model.setDeletedDate(TIME_10_MINUTES_AGO);
        model.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        model.setRecoveryId(VERSIONED_KEY_ENTITY_ID_1_VERSION_1.asRecoveryUri(HTTPS_LOCALHOST_8443).toString());
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
                .add(Arguments.of(List.of(),
                        RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 90, null, null))
                .add(Arguments.of(List.of(),
                        RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 90, TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES))
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

    public static Stream<Arguments> updateAttributeProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null, null))
                .add(Arguments.of(List.of(), TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES, null, TAGS_EMPTY))
                .add(Arguments.of(List.of(), null, TIME_IN_10_MINUTES, null, null))
                .add(Arguments.of(List.of(), null, null, true, TAGS_THREE_KEYS))
                .add(Arguments.of(List.of(KeyOperation.ENCRYPT), TIME_10_MINUTES_AGO, null, false, TAGS_TWO_KEYS))
                .add(Arguments.of(List.of(KeyOperation.ENCRYPT), TIME_IN_10_MINUTES, null, null, TAGS_TWO_KEYS))
                .add(Arguments.of(List.of(KeyOperation.ENCRYPT, KeyOperation.DECRYPT),
                        TIME_10_MINUTES_AGO, TIME_IN_10_MINUTES, false, TAGS_TWO_KEYS))
                .build();
    }

    public static Stream<Arguments> exceptionProvider() {
        final var message = "Message";
        final var failed = "failed";
        return Stream.<Arguments>builder()
                .add(Arguments.of(new IllegalStateException(message),
                        HttpStatus.INTERNAL_SERVER_ERROR, message, null))
                .add(Arguments.of(new NotFoundException(message),
                        HttpStatus.NOT_FOUND, message, null))
                .add(Arguments.of(new AlreadyExistsException(message),
                        HttpStatus.CONFLICT, message, null))
                .add(Arguments.of(new CryptoException(message, new RuntimeException(failed)),
                        HttpStatus.INTERNAL_SERVER_ERROR, message, failed))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(vaultService.findByUri(HTTPS_LOCALHOST_8443)).thenReturn(vaultFake);
        when(vaultFake.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        when(vaultFake.keyVaultFake()).thenReturn(keyVaultFake);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("exceptionProvider")
    void testErrorHandlerConvertsExceptionWhenCaught(
            final Exception exception,
            final HttpStatus status,
            final String message,
            final String innerMessage) {
        //given

        //when
        final var actual = underTest.handleException(exception);

        //then
        Assertions.assertEquals(status, actual.getStatusCode());
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertNotNull(actualBody.error());
        Assertions.assertEquals(message, actualBody.error().getMessage());
        Assertions.assertEquals(exception.getClass().getName(), actualBody.error().getCode());
        final var actualInnerError = actualBody.error().getInnerError();
        if (innerMessage != null) {
            Assertions.assertNotNull(actualInnerError);
            Assertions.assertEquals(exception.getCause().getClass().getName(), actualInnerError.getCode());
            Assertions.assertEquals(innerMessage, actualInnerError.getMessage());
        } else {
            Assertions.assertNull(actualInnerError);
        }
    }

    @Test
    void testErrorHandlerConvertsIllegalArgumentExceptionWhenCaught() {
        //given
        final var status = HttpStatus.BAD_REQUEST;
        final var message = "Message";
        final Exception exception = new IllegalArgumentException(message);

        //when
        final var actual = underTest.handleArgumentException(exception);

        //then
        Assertions.assertEquals(status, actual.getStatusCode());
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertNotNull(actualBody.error());
        Assertions.assertEquals(message, actualBody.error().getMessage());
        Assertions.assertEquals(exception.getClass().getName(), actualBody.error().getCode());
        final var actualInnerError = actualBody.error().getInnerError();
        Assertions.assertNull(actualInnerError);
    }

    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testCreateShouldUseInputParametersWhenCalled(
            final List<KeyOperation> operations,
            @Nullable final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        when(vaultFake.getRecoveryLevel())
                .thenReturn(RecoveryLevel.PURGEABLE);
        when(vaultFake.getRecoverableDays())
                .thenReturn(null);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(keyVaultFake.createKeyVersion(eq(KEY_NAME_1), any(KeyCreateDetailedInput.class)))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_1);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        if (recoveryLevel != null) {
            when(vaultFake.getRecoveryLevel())
                    .thenReturn(recoveryLevel);
        }
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final var actual = underTest.create(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2, request);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(keyVaultFake).createKeyVersion(eq(KEY_NAME_1), any(KeyCreateDetailedInput.class));
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @Test
    void testVersionsShouldThrowExceptionWhenKeyIsNotFound() {
        //given
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null)))
                .thenThrow(new NotFoundException("not found"));

        //when
        Assertions.assertThrows(NotFoundException.class,
                () -> underTest.versions(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2, 0, 0));

        //then + exception
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testVersionsShouldFilterTheListReturnedWhenKeyIsFoundAndHasMoreVersionsThanNeeded() {
        //given
        final var index = 30;
        final var fullList = IntStream.range(0, 42)
                .mapToObj(i -> UUID.randomUUID().toString().replaceAll("-", ""))
                .sorted()
                .collect(Collectors.toCollection(LinkedList::new));
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final var expectedNextUri = baseUri.asUri(HTTPS_LOCALHOST_8443, "versions?api-version=7.2&$skiptoken=31&maxresults=1")
                .toString();
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(baseUri)).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final var keyEntityId = invocation.getArgument(0, VersionedKeyEntityId.class);
            return createEntity(keyEntityId, createRequest(null, null, null));
        });
        when(keyEntityToV72KeyItemModelConverter.convert(any(), any())).thenAnswer(invocation -> {
            final KeyVaultKeyEntity<?, ?> entity = invocation.getArgument(0, KeyVaultKeyEntity.class);
            return keyVaultKeyItemModel(entity.getId().asUri(HTTPS_LOCALHOST_8443), Map.of());
        });
        final var expected = new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, fullList.get(index)).asUri(HTTPS_LOCALHOST_8443);

        //when
        final var actual =
                underTest.versions(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2, 1, index);

        //then
        Assertions.assertNotNull(actual);
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals(expected.toString(), actualBody.getValue().getFirst().getKeyId());
        Assertions.assertNotNull(actualBody.getNextLink());
        Assertions.assertEquals(expectedNextUri, actualBody.getNextLink());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testVersionsShouldNotContainNextUriWhenLastPageIsReturnedFully() {
        //given
        final var fullList = IntStream.range(0, 25)
                .mapToObj(i -> UUID.randomUUID().toString().replaceAll("-", ""))
                .sorted()
                .collect(Collectors.toCollection(LinkedList::new));
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getVersions(baseUri)).thenReturn(fullList);
        when(entities.getReadOnlyEntity(any())).thenAnswer(invocation -> {
            final var keyEntityId = invocation.getArgument(0, VersionedKeyEntityId.class);
            return createEntity(keyEntityId, createRequest(null, null, null));
        });
        when(keyEntityToV72KeyItemModelConverter.convert(any(), any())).thenAnswer(invocation -> {
            final KeyVaultKeyEntity<?, ?> entity = invocation.getArgument(0, KeyVaultKeyEntity.class);
            return keyVaultKeyItemModel(entity.getId().asUri(HTTPS_LOCALHOST_8443), Map.of());
        });
        final var expected = fullList.stream()
                .map(e -> new VersionedKeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, e).asUri(HTTPS_LOCALHOST_8443))
                .toList();

        //when
        final var actual =
                underTest.versions(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2, 25, 0);

        //then
        Assertions.assertNotNull(actual);
        final var actualBody = actual.getBody();
        Assertions.assertNotNull(actualBody);
        final var actualList = actualBody.getValue().stream()
                .map(KeyVaultKeyItemModel::getKeyId)
                .map(URI::create)
                .toList();
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertIterableEquals(expected, actualList);
        Assertions.assertNull(actualBody.getNextLink());
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testDeleteKeyShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(keyVaultFake.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(keyVaultFake).delete(baseUri);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(deletedEntities.getLatestVersionOfEntity(baseUri))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(deletedEntities.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final var actual = underTest.delete(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        final var inOrder = inOrder(keyVaultFake);
        inOrder.verify(keyVaultFake).delete(baseUri);
        inOrder.verify(keyVaultFake, atLeastOnce()).getDeletedEntities();
        verify(keyVaultFake, never()).getEntities();
        verify(deletedEntities).getLatestVersionOfEntity(baseUri);
        verify(deletedEntities).getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        verify(keyEntityToV72ModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testRecoverDeletedKeyShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(keyVaultFake.getDeletedEntities())
                .thenReturn(deletedEntities);
        doNothing().when(keyVaultFake).delete(baseUri);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity(baseUri))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final var actual = underTest.recoverDeletedKey(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        final var inOrder = inOrder(keyVaultFake);
        inOrder.verify(keyVaultFake).recover(baseUri);
        inOrder.verify(keyVaultFake, atLeastOnce()).getEntities();
        verify(keyVaultFake, never()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(baseUri);
        verify(entities).getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        verify(keyEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetDeletedKeyShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity(baseUri))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(DELETED_RESPONSE);

        //when
        final var actual = underTest.getDeletedKey(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(DELETED_RESPONSE, actual.getBody());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake, never()).getEntities();
        verify(keyVaultFake, atLeastOnce()).getDeletedEntities();
        verify(entities).getLatestVersionOfEntity(baseUri);
        verify(entities).getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        verify(keyEntityToV72ModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getLatestVersionOfEntity(baseUri))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(entities.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final var actual = underTest.get(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake, atLeastOnce()).getEntities();
        verify(entities).getLatestVersionOfEntity(baseUri);
        verify(entities).getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        verify(keyEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetKeysShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final var keyItemModel = keyVaultKeyItemModel(baseUri.asUri(HTTPS_LOCALHOST_8443), Map.of());
        when(keyEntityToV72KeyItemModelConverter.convertWithoutVersion(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(keyItemModel);

        //when
        final var actual =
                underTest.listKeys(HTTPS_LOCALHOST_8443, V_7_2, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().getFirst());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake, atLeastOnce()).getEntities();
        verify(keyVaultFake, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convertWithoutVersion(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetKeysShouldReturnNextLinkWhenNotOnLastPage(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final var keyItemModel = keyVaultKeyItemModel(baseUri.asUri(HTTPS_LOCALHOST_8443), Map.of());
        when(keyEntityToV72KeyItemModelConverter.convertWithoutVersion(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(keyItemModel);

        //when
        final var actual =
                underTest.listKeys(HTTPS_LOCALHOST_8443, V_7_2, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().getFirst());
        final var expectedNextLink = HTTPS_LOCALHOST_8443 + "/keys?api-version=7.2&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake, atLeastOnce()).getEntities();
        verify(keyVaultFake, never()).getDeletedEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convertWithoutVersion(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetDeletedKeysShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity));
        final var keyItemModel = deletedKeyVaultKeyItemModel(baseUri, Map.of());
        when(keyEntityToV72KeyItemModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(keyItemModel);

        //when
        final var actual =
                underTest.listDeletedKeys(HTTPS_LOCALHOST_8443, V_7_2, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().getFirst());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake, atLeastOnce()).getDeletedEntities();
        verify(keyVaultFake, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testPurgeDeletedShouldRemoveEntryWhenDeletedKeyIsPurgeable(
            final List<KeyOperation> operations,
            @Nullable final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        when(keyVaultFake.getDeletedEntities())
                .thenReturn(entities);
        if (recoveryLevel != null) {
            when(vaultFake.getRecoveryLevel())
                    .thenReturn(recoveryLevel);
        }
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        final var nonNullRecoveryLevel = Objects.requireNonNullElse(recoveryLevel, RecoveryLevel.PURGEABLE);
        if (!nonNullRecoveryLevel.isPurgeable()) {
            doThrow(IllegalStateException.class).when(keyVaultFake).purge(UNVERSIONED_KEY_ENTITY_ID_1);
        }

        //when
        if (nonNullRecoveryLevel.isPurgeable()) {
            final var response = underTest.purgeDeleted(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2);
            Assertions.assertNotNull(response);
            Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        } else {
            Assertions.assertThrows(IllegalStateException.class, () -> underTest.purgeDeleted(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_2));
        }

        //then
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake, never()).getDeletedEntities();
        verify(keyVaultFake, atLeastOnce()).purge(UNVERSIONED_KEY_ENTITY_ID_1);
        verify(keyVaultFake, never()).getEntities();
        verify(entities, never()).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter, never()).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetDeletedKeysShouldReturnNextLinkWhenNotOnLastPage(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultFake.getDeletedEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setDeletedDate(TIME_10_MINUTES_AGO);
        entity.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        when(entities.listLatestEntities())
                .thenReturn(List.of(entity, entity, entity));
        final var keyItemModel = deletedKeyVaultKeyItemModel(baseUri, Map.of());
        when(keyEntityToV72KeyItemModelConverter.convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(keyItemModel);

        //when
        final var actual =
                underTest.listDeletedKeys(HTTPS_LOCALHOST_8443, V_7_2, 1, 0);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertNotNull(actual.getBody().getValue());
        Assertions.assertEquals(1, actual.getBody().getValue().size());
        Assertions.assertSame(keyItemModel, actual.getBody().getValue().getFirst());
        final var expectedNextLink = HTTPS_LOCALHOST_8443 + "/deletedkeys?api-version=7.2&$skiptoken=1&maxresults=1";
        Assertions.assertEquals(expectedNextLink, actual.getBody().getNextLink());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake, atLeastOnce()).getDeletedEntities();
        verify(keyVaultFake, never()).getEntities();
        verify(entities).listLatestEntities();
        verify(keyEntityToV72KeyItemModelConverter).convertDeleted(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetWithVersionShouldReturnEntryWhenKeyAndVersionIsFound(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel,
            final Integer recoverableDays,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final var request = createRequest(operations, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(recoveryLevel);
        when(vaultFake.getRecoverableDays())
                .thenReturn(recoverableDays);
        when(entities.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final var actual = underTest.getWithVersion(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, V_7_2);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(baseUri);
        verify(entities).getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        verify(keyEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("updateAttributeProvider")
    void testUpdateVersionShouldReturnEntryWhenKeyAndVersionIsFound(
            final List<KeyOperation> operations,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore,
            final Boolean enabled,
            final Map<String, String> tags) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final var createKeyRequest = createRequest(null, null, null);
        final var updateKeyRequest = new UpdateKeyRequest();
        if (operations != null) {
            updateKeyRequest.setKeyOperations(operations);
        }
        if (tags != null) {
            updateKeyRequest.setTags(tags);
        }
        if (enabled != null || expiry != null || notBefore != null) {
            final var properties = new BasePropertiesUpdateModel();
            properties.setEnabled(enabled);
            properties.setExpiresOn(expiry);
            properties.setNotBefore(notBefore);
            updateKeyRequest.setProperties(properties);
        }
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, createKeyRequest);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(vaultFake.getRecoveryLevel())
                .thenReturn(RecoveryLevel.PURGEABLE);
        when(entities.getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);

        //when
        final var actual = underTest
                .updateVersion(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, V_7_2, updateKeyRequest);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(RESPONSE, actual.getBody());
        verify(vaultService).findByUri(HTTPS_LOCALHOST_8443);
        verify(vaultFake).keyVaultFake();
        verify(vaultFake).getRecoveryLevel();
        verify(vaultFake).getRecoverableDays();
        verify(keyVaultFake).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(baseUri);
        final var inOrder = inOrder(keyVaultFake, entities);
        if (operations != null) {
            final var keyOperations = Objects.requireNonNull(updateKeyRequest.getKeyOperations());
            inOrder.verify(keyVaultFake)
                    .setKeyOperations(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3), same(keyOperations));
        } else {
            inOrder.verify(keyVaultFake, never())
                    .setKeyOperations(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3), anyList());
        }
        if (enabled != null) {
            inOrder.verify(keyVaultFake)
                    .setEnabled(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, enabled);
        } else {
            inOrder.verify(keyVaultFake, never())
                    .setEnabled(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3), anyBoolean());
        }
        if (expiry != null || notBefore != null) {
            inOrder.verify(keyVaultFake)
                    .setExpiry(VERSIONED_KEY_ENTITY_ID_1_VERSION_3, notBefore, expiry);
        } else {
            inOrder.verify(keyVaultFake, never())
                    .setExpiry(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3), any(), any());
        }
        if (tags != null) {
            inOrder.verify(keyVaultFake)
                    .clearTags(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
            inOrder.verify(keyVaultFake)
                    .addTags(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3), same(updateKeyRequest.getTags()));
        } else {
            inOrder.verify(keyVaultFake, never())
                    .clearTags(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
            inOrder.verify(keyVaultFake, never())
                    .addTags(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3), anyMap());
        }
        inOrder.verify(entities).getReadOnlyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        verify(keyEntityToV72ModelConverter).convert(same(entity), eq(HTTPS_LOCALHOST_8443));
    }

    private CreateKeyRequest createRequest(
            final List<KeyOperation> operations,
            final OffsetDateTime expiry,
            final OffsetDateTime notBefore) {
        final var keyRequest = new CreateKeyRequest();
        keyRequest.setKeyType(KeyType.RSA);
        keyRequest.setKeyOperations(operations);
        final var properties = new KeyPropertiesModel();
        properties.setExpiry(expiry);
        properties.setNotBefore(notBefore);
        properties.setEnabled(true);
        keyRequest.setProperties(properties);
        keyRequest.setTags(TAGS_TWO_KEYS);
        return keyRequest;
    }

    private KeyVaultKeyEntity<?, ?> createEntity(
            final VersionedKeyEntityId keyEntityId,
            final CreateKeyRequest createKeyRequest) {
        return new RsaKeyVaultKeyEntity(keyEntityId, vaultFake, createKeyRequest.getKeySize(), null, false);
    }

    private KeyVaultKeyItemModel keyVaultKeyItemModel(
            final URI asUriNoVersion,
            final Map<String, String> tags) {
        final var model = new KeyVaultKeyItemModel();
        model.setAttributes(new KeyPropertiesModel());
        model.setKeyId(asUriNoVersion.toString());
        model.setTags(tags);
        return model;
    }

    private DeletedKeyVaultKeyItemModel deletedKeyVaultKeyItemModel(
            final KeyEntityId id,
            final Map<String, String> tags) {
        final var model = new DeletedKeyVaultKeyItemModel();
        model.setAttributes(new KeyPropertiesModel());
        model.setKeyId(id.asUriNoVersion(id.vault()).toString());
        model.setTags(tags);
        model.setDeletedDate(TIME_10_MINUTES_AGO);
        model.setScheduledPurgeDate(TIME_IN_10_MINUTES);
        model.setRecoveryId(id.asRecoveryUri(id.vault()).toString());
        return model;
    }
}
