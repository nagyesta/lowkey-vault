package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.constants.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.EncryptionAlgorithm;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.service.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultStub;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
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
import org.mockito.InjectMocks;
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
    private static final KeyVaultKeyModel RESPONSE = new KeyVaultKeyModel(new KeyPropertiesModel(), new JsonWebKeyModel(), Map.of());
    @Mock
    private KeyEntityToV72ModelConverter keyEntityToV72ModelConverter;
    @Mock
    private KeyEntityToV72ItemModelConverter keyEntityToV72ItemModelConverter;
    @Mock
    private VaultService vaultService;
    @Mock
    private VaultStub vaultStub;
    @Mock
    private KeyVaultStub keyVaultStub;
    @InjectMocks
    private KeyController underTest;

    private AutoCloseable openMocks;

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
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(mock(KeyEntityToV72ModelConverter.class), null, null))
                .add(Arguments.of(null, mock(KeyEntityToV72ItemModelConverter.class), null))
                .add(Arguments.of(null, null, mock(VaultService.class)))
                .add(Arguments.of(null, mock(KeyEntityToV72ItemModelConverter.class), mock(VaultService.class)))
                .add(Arguments.of(mock(KeyEntityToV72ModelConverter.class), null, mock(VaultService.class)))
                .add(Arguments.of(mock(KeyEntityToV72ModelConverter.class), mock(KeyEntityToV72ItemModelConverter.class), null))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
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
            final KeyEntityToV72ItemModelConverter keyEntityToV72ItemModelConverter,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyController(keyEntityToV72ModelConverter, keyEntityToV72ItemModelConverter, vaultService));

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
        final CreateKeyRequest request = createRequest(operations, recoveryLevel, recoverableDays, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(keyVaultStub.createKeyVersion(eq(KEY_NAME_1), eq(request.toKeyCreationInput())))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_1);
        when(keyVaultStub.getEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1)))
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
        if (recoveryLevel != null) {
            verify(keyVaultStub).setRecovery(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), eq(recoveryLevel), eq(recoverableDays));
        } else {
            verify(keyVaultStub, never()).setRecovery(any(), any(), any());
        }
        verify(keyVaultStub).setExpiry(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), eq(notBefore), eq(expiry));
        verify(keyVaultStub).setEnabled(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), eq(true));
        verify(keyVaultStub).addTags(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_1), same(TAGS_TWO_KEYS));
        verify(keyEntityToV72ModelConverter).convert(same(entity));
    }

    @Test
    void testVersionsShouldThrowExceptionWhenKeyIsNotFound() {
        //given
        when(keyVaultStub.getVersions(eq(new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null))))
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
        when(keyVaultStub.getVersions(eq(baseUri))).thenReturn(fullList);
        when(keyVaultStub.getEntity(any())).thenAnswer(invocation -> {
            final VersionedKeyEntityId keyEntityId = invocation.getArgument(0, VersionedKeyEntityId.class);
            return createEntity(keyEntityId, createRequest(null, null, null, null, null));
        });
        when(keyEntityToV72ItemModelConverter.convert(any())).thenAnswer(invocation -> {
            final KeyVaultKeyEntity<?, ?> entity = invocation.getArgument(0, KeyVaultKeyEntity.class);
            return new KeyVaultKeyItemModel(new KeyPropertiesModel(), entity.getId().asUri(), Map.of());
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
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetShouldReturnEntryWhenKeyIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        when(keyVaultStub.getLatestVersionOfEntity((eq(baseUri))))
                .thenReturn(VERSIONED_KEY_ENTITY_ID_1_VERSION_3);
        final CreateKeyRequest request = createRequest(operations, recoveryLevel, recoverableDays, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(keyVaultStub.getEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
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
        verify(keyVaultStub).getLatestVersionOfEntity(eq(baseUri));
        verify(keyVaultStub).getEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
        verify(keyEntityToV72ModelConverter).convert(same(entity));
    }


    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @MethodSource("keyAttributeProvider")
    void testGetWithVersionShouldReturnEntryWhenKeyAndVersionIsFound(
            final List<KeyOperation> operations, final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        //given
        final KeyEntityId baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final CreateKeyRequest request = createRequest(operations, recoveryLevel, recoverableDays, expiry, notBefore);
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        when(keyVaultStub.getEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
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
        verify(keyVaultStub, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(keyVaultStub).getEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
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
        final CreateKeyRequest request = createRequest(operations, null, null, null, null);
        final RsaKeyVaultKeyEntity entity = (RsaKeyVaultKeyEntity) createEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1, request);
        entity.setEnabled(true);
        entity.setOperations(operations);
        when(keyVaultStub.getEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
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
        verify(keyVaultStub, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(keyVaultStub, times(2)).getEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
    }

    @NonNull
    private CreateKeyRequest createRequest(
            final List<KeyOperation> operations,
            final RecoveryLevel recoveryLevel, final Integer recoverableDays,
            final OffsetDateTime expiry, final OffsetDateTime notBefore) {
        final CreateKeyRequest keyRequest = new CreateKeyRequest();
        keyRequest.setKeyType(KeyType.RSA);
        keyRequest.setKeyOperations(operations);
        final KeyPropertiesModel properties = new KeyPropertiesModel();
        properties.setRecoveryLevel(recoveryLevel);
        properties.setRecoverableDays(recoverableDays);
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
}
