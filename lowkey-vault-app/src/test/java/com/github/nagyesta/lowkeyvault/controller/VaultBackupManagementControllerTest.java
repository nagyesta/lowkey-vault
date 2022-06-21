package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.SecretBackupRestoreController;
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
        final VaultImporter importer = mock(VaultImporter.class);
        final VaultService service = mock(VaultService.class);
        final VaultManagementController management = mock(VaultManagementController.class);
        final KeyBackupRestoreController key = mock(KeyBackupRestoreController.class);
        final SecretBackupRestoreController secret = mock(SecretBackupRestoreController.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, null, null, null, null))
                .add(Arguments.of(importer, null, null, null, null))
                .add(Arguments.of(null, service, null, null, null))
                .add(Arguments.of(null, null, management, null, null))
                .add(Arguments.of(null, null, null, key, null))
                .add(Arguments.of(null, null, null, null, secret))
                .add(Arguments.of(null, service, management, key, secret))
                .add(Arguments.of(importer, null, management, key, secret))
                .add(Arguments.of(importer, service, null, key, secret))
                .add(Arguments.of(importer, service, management, null, secret))
                .add(Arguments.of(importer, service, management, key, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VaultImporter vaultImporter,
            final VaultService vaultService,
            final VaultManagementController vaultManagementController,
            final KeyBackupRestoreController keyBackupRestoreController,
            final SecretBackupRestoreController secretBackupRestoreController) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new VaultBackupManagementController(vaultImporter, vaultService,
                        vaultManagementController, keyBackupRestoreController, secretBackupRestoreController));

        //then + exception
    }
}
