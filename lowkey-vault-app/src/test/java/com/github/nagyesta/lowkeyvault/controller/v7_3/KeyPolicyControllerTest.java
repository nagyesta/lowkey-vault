package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
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
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsKeys.KEY_NAME_1;
import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.mockito.Mockito.*;

class KeyPolicyControllerTest {

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
    @InjectMocks
    private KeyController underTest;
    private AutoCloseable openMocks;

    public static Stream<Arguments> nullProvider() {
        final var service = mock(VaultService.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(service, null))
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

    @Test
    void testGetRotationPolicyShouldReturnTheRotationPolicyWhenItIsAlreadySet() {
        //given
        final var entityId = new KeyEntityId(HTTPS_LOCALHOST_8443, KEY_NAME_1);
        final var rotationPolicy = mock(KeyRotationPolicy.class);
        final var model = mock(KeyRotationPolicyModel.class);
        when(keyVaultFake.rotationPolicy(entityId))
                .thenReturn(rotationPolicy);
        when(rotationPolicyModelConverter.convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(model);

        //when
        final var actual = underTest.getRotationPolicy(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_3);

        //then
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(model, actual.getBody());
        final var inOrder = inOrder(keyVaultFake, rotationPolicyModelConverter);
        inOrder.verify(keyVaultFake).rotationPolicy(entityId);
        inOrder.verify(rotationPolicyModelConverter).convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443));
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
        when(rotationPolicyEntityConverter.convert(same(input)))
                .thenReturn(rotationPolicy);
        when(rotationPolicyModelConverter.convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443)))
                .thenReturn(output);

        //when
        final var actual = underTest.updateRotationPolicy(KEY_NAME_1, HTTPS_LOCALHOST_8443, V_7_3, input);

        //then
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertSame(output, actual.getBody());
        final var inOrder = inOrder(keyVaultFake, rotationPolicyEntityConverter, rotationPolicyModelConverter);
        inOrder.verify(rotationPolicyEntityConverter).convert(same(input));
        inOrder.verify(keyVaultFake).setRotationPolicy(same(rotationPolicy));
        inOrder.verify(keyVaultFake).rotationPolicy(entityId);
        inOrder.verify(rotationPolicyModelConverter).convert(same(rotationPolicy), eq(HTTPS_LOCALHOST_8443));
    }
}
