package com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate;

import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class CertificateEntityToV73BackupConverterTest {

    public static Stream<Arguments> nullProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null))
                .add(Arguments.of(null, mock(VaultService.class)))
                .add(Arguments.of(new CertificateConverterRegistry(), null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final CertificateConverterRegistry registry, final VaultService vaultService) {
        //given

        //when
        assertThrows(IllegalArgumentException.class, () -> new CertificateEntityToV73BackupConverter(registry, vaultService));

        //then + exception
    }

}
