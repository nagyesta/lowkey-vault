package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyRotationPolicy;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
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

import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_VERSION_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class KeyPolicyControllerTest {

    @Mock
    private VaultService vaultService;
    @Mock
    private VaultFake vaultFake;
    @Mock
    private KeyVaultFake keyVaultFake;
    @Mock
    private KeyRotationPolicyToV73ModelConverter keyRotationPolicyToV73ModelConverter;
    @Mock
    private KeyRotationPolicyV73ModelToEntityConverter rotationV73ModelToEntityConverter;
    private KeyPolicyController underTest;
    private AutoCloseable openMocks;

    public static Stream<Arguments> nullProvider() {
        final VaultService service = mock(VaultService.class);
        final KeyRotationPolicyToV73ModelConverter entityConverter = mock(KeyRotationPolicyToV73ModelConverter.class);
        final KeyRotationPolicyV73ModelToEntityConverter modelConverter = mock(KeyRotationPolicyV73ModelToEntityConverter.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null))
                .add(Arguments.of(service, null, null))
                .add(Arguments.of(null, entityConverter, null))
                .add(Arguments.of(null, null, modelConverter))
                .add(Arguments.of(null, entityConverter, modelConverter))
                .add(Arguments.of(service, null, modelConverter))
                .add(Arguments.of(service, entityConverter, null))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        underTest = new KeyPolicyController(vaultService, keyRotationPolicyToV73ModelConverter, rotationV73ModelToEntityConverter);
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
            final VaultService service,
            final KeyRotationPolicyToV73ModelConverter entityConverter,
            final KeyRotationPolicyV73ModelToEntityConverter modelConverter) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyPolicyController(service, entityConverter, modelConverter));

        //then + exception
    }

    @Test
    void testGetRotationPolicyShouldReturnTheRotationPolicyWhenItIsAlreadySet() {
        //given
        final KeyEntityId entityId = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1);
        final KeyRotationPolicy rotationPolicy = mock(KeyRotationPolicy.class);
        final KeyRotationPolicyModel model = mock(KeyRotationPolicyModel.class);
        when(keyVaultFake.rotationPolicy(eq(entityId)))
                .thenReturn(rotationPolicy);
        when(keyRotationPolicyToV73ModelConverter.convert(same(rotationPolicy)))
                .thenReturn(model);

        //when
        final ResponseEntity<KeyRotationPolicyModel> actual = underTest.getRotationPolicy(KEY_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(model, actual.getBody());
        final InOrder inOrder = inOrder(keyVaultFake, keyRotationPolicyToV73ModelConverter);
        inOrder.verify(keyVaultFake).rotationPolicy(eq(entityId));
        inOrder.verify(keyRotationPolicyToV73ModelConverter).convert(same(rotationPolicy));
    }

    @Test
    void testUpdateRotationPolicyShouldReturnTheRotationPolicyWhenItIsAlreadySet() {
        //given
        final KeyEntityId entityId = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1);
        final KeyRotationPolicy rotationPolicy = mock(KeyRotationPolicy.class);
        final KeyRotationPolicyModel input = mock(KeyRotationPolicyModel.class);
        final KeyRotationPolicyModel output = mock(KeyRotationPolicyModel.class);
        when(keyVaultFake.rotationPolicy(eq(entityId)))
                .thenReturn(rotationPolicy);
        when(rotationV73ModelToEntityConverter.convert(eq(entityId), same(input)))
                .thenReturn(rotationPolicy);
        when(keyRotationPolicyToV73ModelConverter.convert(same(rotationPolicy)))
                .thenReturn(output);

        //when
        final ResponseEntity<KeyRotationPolicyModel> actual = underTest.updateRotationPolicy(KEY_NAME_1, HTTPS_LOCALHOST_8443, input);

        //then
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(output, actual.getBody());
        final InOrder inOrder = inOrder(keyVaultFake, rotationV73ModelToEntityConverter, keyRotationPolicyToV73ModelConverter);
        inOrder.verify(rotationV73ModelToEntityConverter).convert(eq(entityId), same(input));
        inOrder.verify(keyVaultFake).setRotationPolicy(same(rotationPolicy));
        inOrder.verify(keyVaultFake).rotationPolicy(eq(entityId));
        inOrder.verify(keyRotationPolicyToV73ModelConverter).convert(same(rotationPolicy));
    }

    @Test
    void testVersionedEntityIdShouldReturnAVersionedKeyEntityIdWhenCalledWithValidInput() {
        //given

        //when
        final VersionedKeyEntityId actual = underTest.versionedEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1, KEY_VERSION_1);

        //then
        Assertions.assertEquals(HTTPS_LOCALHOST_8443, actual.vault());
        Assertions.assertEquals(KEY_NAME_1, actual.id());
        Assertions.assertEquals(KEY_VERSION_1, actual.version());
    }
}
