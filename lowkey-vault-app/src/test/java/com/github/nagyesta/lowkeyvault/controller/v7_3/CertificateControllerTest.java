package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class CertificateControllerTest {

    public static Stream<Arguments> nullProvider() {
        final CertificateConverterRegistry registry = mock(CertificateConverterRegistry.class);
        final VaultService vaultService = mock(VaultService.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(registry, null))
                .add(Arguments.of(null, vaultService))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNulls(
            final CertificateConverterRegistry registry,
            final VaultService vaultService) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> new CertificateController(registry, vaultService));

        //then + exception
    }
}
