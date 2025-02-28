package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.HashUtil;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.JsonWebKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeySignParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyVerifyParameters;
import com.github.nagyesta.lowkeyvault.service.common.ReadOnlyVersionedEntityMultiMap;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.impl.RsaKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.*;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.*;

class KeyCryptoControllerTest {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();
    private static final KeyVaultKeyModel RESPONSE = createResponse();
    @Mock
    private KeyEntityToV72ModelConverter keyEntityToV72ModelConverter;
    @Mock
    private KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter;
    @Mock
    private KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter;
    @Mock
    private VaultService vaultService;
    @Mock
    private VaultFake vaultFake;
    @Mock
    private KeyVaultFake keyVaultFake;
    @Mock
    private KeyConverterRegistry registry;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> entities;
    private KeyCryptoController underTest;
    private AutoCloseable openMocks;

    private static KeyVaultKeyModel createResponse() {
        final var model = new KeyVaultKeyModel();
        model.setKey(new JsonWebKeyModel());
        model.setAttributes(new KeyPropertiesModel());
        model.setTags(Map.of());
        return model;
    }

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(mock(KeyConverterRegistry.class), null))
                .add(Arguments.of(null, mock(VaultService.class)))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(registry.modelConverter(eq(ApiConstants.V_7_2))).thenReturn(keyEntityToV72ModelConverter);
        when(registry.itemConverter(eq(ApiConstants.V_7_2))).thenReturn(keyEntityToV72KeyItemModelConverter);
        when(registry.versionedItemConverter(eq(ApiConstants.V_7_2))).thenReturn(keyEntityToV72KeyVersionItemModelConverter);
        when(registry.versionedEntityId(any(URI.class), anyString(), anyString())).thenCallRealMethod();
        when(registry.entityId(any(URI.class), anyString())).thenCallRealMethod();
        underTest = new KeyCryptoController(registry, vaultService);
        when(vaultService.findByUri(eq(HTTPS_LOCALHOST_8443))).thenReturn(vaultFake);
        when(vaultFake.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        when(vaultFake.keyVaultFake()).thenReturn(keyVaultFake);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final KeyConverterRegistry registry,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyCryptoController(registry, vaultService));

        //then + exception
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @ValueSource(strings = {BLANK, DEFAULT_VAULT, LOCALHOST, LOWKEY_VAULT})
    void testEncryptAndDecryptShouldGetBackOriginalInputWhenKeyAndVersionIsFound(final String clearText) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final var operations = List.of(
                KeyOperation.ENCRYPT, KeyOperation.DECRYPT, KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY);
        final var request = createRequest(operations);
        final var entity = (RsaKeyVaultKeyEntity) createEntity(request);
        entity.setEnabled(true);
        entity.setOperations(operations);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);
        final var encryptParameters = new KeyOperationsParameters();
        encryptParameters.setAlgorithm(EncryptionAlgorithm.RSA_OAEP_256);
        encryptParameters.setValue(ENCODER.encodeToString(clearText.getBytes(StandardCharsets.UTF_8)));

        //when
        final var encrypted = underTest
                .encrypt(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, encryptParameters);
        Assertions.assertNotNull(encrypted);
        Assertions.assertEquals(HttpStatus.OK, encrypted.getStatusCode());
        Assertions.assertNotNull(encrypted.getBody());
        Assertions.assertNotEquals(clearText, encrypted.getBody().getValue());

        final var decryptParameters = new KeyOperationsParameters();
        decryptParameters.setAlgorithm(EncryptionAlgorithm.RSA_OAEP_256);
        decryptParameters.setValue(encrypted.getBody().getValue());
        final var actual = underTest
                .decrypt(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, decryptParameters);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        final var decoded = new String(DECODER.decode(actual.getBody().getValue()), StandardCharsets.UTF_8);
        Assertions.assertEquals(clearText, decoded);

        verify(vaultService, times(2)).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake, times(2)).keyVaultFake();
        verify(keyVaultFake, times(2)).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities, times(2)).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @ParameterizedTest
    @ValueSource(strings = {BLANK, DEFAULT_VAULT, LOCALHOST, LOWKEY_VAULT})
    void testSignAndVerifyShouldReturnTrueWhenKeyAndVersionIsFoundAndCalledInSequence(final String clearText) {
        //given
        final var baseUri = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final var operations = List.of(KeyOperation.SIGN, KeyOperation.VERIFY);
        final var request = createRequest(operations);
        final var entity = (RsaKeyVaultKeyEntity) createEntity(request);
        entity.setEnabled(true);
        entity.setOperations(operations);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3)))
                .thenReturn(entity);
        when(keyEntityToV72ModelConverter.convert(same(entity), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(RESPONSE);
        final var keySignParameters = new KeySignParameters();
        keySignParameters.setAlgorithm(SignatureAlgorithm.PS256);
        keySignParameters.setValue(ENCODER.encodeToString(HashUtil.hash(clearText.getBytes(StandardCharsets.UTF_8), HashAlgorithm.SHA256)));

        //when
        final var signature = underTest
                .sign(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, keySignParameters);
        Assertions.assertNotNull(signature);
        Assertions.assertEquals(HttpStatus.OK, signature.getStatusCode());
        Assertions.assertNotNull(signature.getBody());
        Assertions.assertNotEquals(clearText, signature.getBody().getValue());

        final var verifyParameters = new KeyVerifyParameters();
        verifyParameters.setAlgorithm(SignatureAlgorithm.PS256);
        verifyParameters.setDigest(ENCODER.encodeToString(HashUtil.hash(clearText.getBytes(StandardCharsets.UTF_8), HashAlgorithm.SHA256)));
        verifyParameters.setValue(signature.getBody().getValue());
        final var actual = underTest
                .verify(KEY_NAME_1, KEY_VERSION_3, HTTPS_LOCALHOST_8443, verifyParameters);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertTrue(actual.getBody().isValue());

        verify(vaultService, times(2)).findByUri(eq(HTTPS_LOCALHOST_8443));
        verify(vaultFake, times(2)).keyVaultFake();
        verify(keyVaultFake, times(2)).getEntities();
        verify(entities, never()).getLatestVersionOfEntity(eq(baseUri));
        verify(entities, times(2)).getReadOnlyEntity(eq(VERSIONED_KEY_ENTITY_ID_1_VERSION_3));
    }

    @NonNull
    private CreateKeyRequest createRequest(
            final List<KeyOperation> operations) {
        final var keyRequest = new CreateKeyRequest();
        keyRequest.setKeyType(KeyType.RSA);
        keyRequest.setKeyOperations(operations);
        final var properties = new KeyPropertiesModel();
        properties.setExpiresOn(null);
        properties.setNotBefore(null);
        properties.setEnabled(true);
        keyRequest.setProperties(properties);
        keyRequest.setTags(TAGS_TWO_KEYS);
        return keyRequest;
    }

    @NonNull
    private KeyVaultKeyEntity<?, ?> createEntity(final CreateKeyRequest createKeyRequest) {
        return new RsaKeyVaultKeyEntity(VERSIONED_KEY_ENTITY_ID_1_VERSION_1,
                vaultFake, createKeyRequest.getKeySize(), null, false);
    }
}
