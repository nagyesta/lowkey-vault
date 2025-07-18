package com.github.nagyesta.lowkeyvault.controller.v7_6;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.*;

class KeyPolicyControllerTest {

    @Mock
    private VaultService vaultService;
    @Mock
    private VaultFake vaultFake;
    @Mock
    private KeyVaultFake keyVaultFake;
    @Mock
    private KeyConverterRegistry registry;
    @Mock
    private KeyRotationPolicyToV73ModelConverter keyRotationPolicyToV73ModelConverter;
    @Mock
    private KeyRotationPolicyV73ModelToEntityConverter rotationV73ModelToEntityConverter;
    private com.github.nagyesta.lowkeyvault.controller.v7_6.KeyPolicyController underTest;
    private AutoCloseable openMocks;

    public static Stream<Arguments> nullProvider() {
        final var service = mock(VaultService.class);
        final var registry = mock(KeyConverterRegistry.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(service, null))
                .add(Arguments.of(null, registry))
                .build();
    }

    @BeforeEach
    void setUp() {
        openMocks = MockitoAnnotations.openMocks(this);
        when(registry.rotationPolicyModelConverter(ApiConstants.V_7_6)).thenReturn(keyRotationPolicyToV73ModelConverter);
        when(registry.rotationPolicyEntityConverter(ApiConstants.V_7_6)).thenReturn(rotationV73ModelToEntityConverter);
        when(registry.versionedEntityId(any(URI.class), anyString(), anyString())).thenCallRealMethod();
        when(registry.entityId(any(URI.class), anyString())).thenCallRealMethod();
        underTest = new com.github.nagyesta.lowkeyvault.controller.v7_6.KeyPolicyController(registry, vaultService);
        when(vaultService.findByUri(HTTPS_LOCALHOST_8443)).thenReturn(vaultFake);
        when(vaultFake.baseUri()).thenReturn(HTTPS_LOCALHOST_8443);
        when(vaultFake.keyVaultFake()).thenReturn(keyVaultFake);
    }

    @AfterEach
    void tearDown() throws Exception {
        openMocks.close();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(final VaultService service, final KeyConverterRegistry registry) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new KeyPolicyController(registry, service));

        //then + exception
    }

    @Test
    void testGetRotationPolicyShouldReturnTheRotationPolicyWhenItIsAlreadySet() {
        //given
        final var entityId = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1);
        final var rotationPolicy = mock(KeyRotationPolicy.class);
        final var model = mock(KeyRotationPolicyModel.class);
        when(keyVaultFake.rotationPolicy(entityId))
                .thenReturn(rotationPolicy);
        when(keyRotationPolicyToV73ModelConverter.convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(model);

        //when
        final var actual = underTest.getRotationPolicy(KEY_NAME_1, HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(model, actual.getBody());
        final var inOrder = inOrder(keyVaultFake, keyRotationPolicyToV73ModelConverter);
        inOrder.verify(keyVaultFake).rotationPolicy(entityId);
        inOrder.verify(keyRotationPolicyToV73ModelConverter).convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443));
    }

    @Test
    void testUpdateRotationPolicyShouldReturnTheRotationPolicyWhenItIsAlreadySet() {
        //given
        final var entityId = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1);
        final var rotationPolicy = mock(KeyRotationPolicy.class);
        final var input = mock(KeyRotationPolicyModel.class);
        final var output = mock(KeyRotationPolicyModel.class);
        when(keyVaultFake.rotationPolicy(entityId))
                .thenReturn(rotationPolicy);
        when(rotationV73ModelToEntityConverter.convert(same(input)))
                .thenReturn(rotationPolicy);
        when(keyRotationPolicyToV73ModelConverter.convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(output);

        //when
        final var actual = underTest.updateRotationPolicy(KEY_NAME_1, HTTPS_LOCALHOST_8443, input);

        //then
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(output, actual.getBody());
        final var inOrder = inOrder(keyVaultFake, rotationV73ModelToEntityConverter, keyRotationPolicyToV73ModelConverter);
        inOrder.verify(rotationV73ModelToEntityConverter).convert(same(input));
        inOrder.verify(keyVaultFake).setRotationPolicy(same(rotationPolicy));
        inOrder.verify(keyVaultFake).rotationPolicy(entityId);
        inOrder.verify(keyRotationPolicyToV73ModelConverter).convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443));
    }
}
