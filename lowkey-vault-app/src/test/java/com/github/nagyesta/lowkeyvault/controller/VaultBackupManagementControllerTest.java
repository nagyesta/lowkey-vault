package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class VaultBackupManagementControllerTest {

    public static Stream<Arguments> nullProvider() {
        final var importer = mock(VaultImporter.class);
        final var service = mock(VaultService.class);
        final var executor = mock(VaultImportExportExecutor.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, service, executor))
                .add(Arguments.of(importer, null, executor))
                .add(Arguments.of(importer, service, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VaultImporter vaultImporter,
            final VaultService vaultService,
            final VaultImportExportExecutor vaultImportExportExecutor) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new VaultBackupManagementController(vaultImporter, vaultService, vaultImportExportExecutor));

        //then + exception
    }
}
