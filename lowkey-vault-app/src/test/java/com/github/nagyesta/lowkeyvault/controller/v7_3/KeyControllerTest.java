package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.JsonWebKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyPropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.RandomBytesRequest;
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
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

import static com.github.nagyesta.lowkeyvault.TestConstants.TAGS_TWO_KEYS;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.mockito.Mockito.*;

class KeyControllerTest {

    private static final KeyVaultKeyModel RESPONSE = createResponse();
    @Mock
    private VaultService vaultService;
    @Mock
    private VaultFake vaultFake;
    @Mock
    private KeyVaultFake keyVaultFake;
    @Mock
    private KeyEntityToV72ModelConverter modelConverter;
    @Mock
    private KeyEntityToV72KeyItemModelConverter itemConverter;
    @Mock
    private KeyRotationPolicyToV73ModelConverter rotationPolicyModelConverter;
    @Mock
    private KeyRotationPolicyV73ModelToEntityConverter rotationPolicyEntityConverter;
    @Mock
    private ReadOnlyVersionedEntityMultiMap<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity> entities;
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

    @Test
    void testRotateKeyShouldCallTheKeyVaultFakeForDoingTheRotationWhenCalled() {
        //given
        final var entityId = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, null);
        final var newKeyId = new VersionedKeyEntityId(entityId.vault(), entityId.id());
        final var request = createRequest(List.of());
        final ReadOnlyKeyVaultKeyEntity entity = createEntity(newKeyId, request);
        when(keyVaultFake.rotateKey(entityId))
                .thenReturn(newKeyId);
        when(keyVaultFake.getEntities())
                .thenReturn(entities);
        when(entities.getReadOnlyEntity(newKeyId))
                .thenReturn(entity);
        when(modelConverter.convert(same(entity), eq(newKeyId.vault())))
                .thenReturn(RESPONSE);

        //when
        final var actual = underTest.rotateKey(entityId.id(), entityId.vault(), V_7_3);

        //then
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals(RESPONSE, actual.getBody());
        final var inOrder = inOrder(keyVaultFake, entities, modelConverter);
        inOrder.verify(keyVaultFake)
                .rotateKey(entityId);
        inOrder.verify(keyVaultFake)
                .getEntities();
        inOrder.verify(entities)
                .getReadOnlyEntity(newKeyId);
        inOrder.verify(modelConverter)
                .convert(same(entity), eq(newKeyId.vault()));
    }

    private CreateKeyRequest createRequest(final List<KeyOperation> operations) {
        final var keyRequest = new CreateKeyRequest();
        keyRequest.setKeyType(KeyType.RSA);
        keyRequest.setKeyOperations(operations);
        final var properties = new KeyPropertiesModel();
        properties.setExpiry(null);
        properties.setNotBefore(null);
        properties.setEnabled(true);
        keyRequest.setProperties(properties);
        keyRequest.setTags(TAGS_TWO_KEYS);
        return keyRequest;
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Test
    void testGetRandomBytesShouldReturnRandomBytesWhenCalledWithValidData() {
        //given
        final var request = new RandomBytesRequest();
        request.setCount(128);

        //when
        final var response = underTest.getRandomBytes(V_7_3, request);

        //then
        final var body = response.getBody();
        Assertions.assertNotNull(body);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(128, body.getValue().length);
    }

    private KeyVaultKeyEntity<?, ?> createEntity(
            final VersionedKeyEntityId keyEntityId,
            final CreateKeyRequest createKeyRequest) {
        return new RsaKeyVaultKeyEntity(keyEntityId, vaultFake, createKeyRequest.getKeySize(), null, false);
    }
}
