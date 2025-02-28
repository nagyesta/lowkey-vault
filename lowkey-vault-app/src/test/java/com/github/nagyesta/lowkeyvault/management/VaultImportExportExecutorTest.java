package com.github.nagyesta.lowkeyvault.management;

import com.github.nagyesta.lowkeyvault.controller.VaultManagementController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.CertificateBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.SecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;
import static org.mockito.Mockito.mock;

class VaultImportExportExecutorTest {

    public static Stream<Arguments> nullProvider() {
        final var management = mock(VaultManagementController.class);
        final var key = mock(KeyBackupRestoreController.class);
        final var secret = mock(SecretBackupRestoreController.class);
        final var certificate = mock(CertificateBackupRestoreController.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, key, secret, certificate))
                .add(Arguments.of(management, null, secret, certificate))
                .add(Arguments.of(management, key, null, certificate))
                .add(Arguments.of(management, key, secret, null))
                .build();
    }

    public static Stream<Arguments> restoreNullProvider() {
        final var importer = mock(VaultImporter.class);
        final var baseUri = HTTPS_LOCALHOST_8443;
        final var vault = mock(VaultModel.class);
        return Stream.<Arguments>builder()
                .add(Arguments.of(null, baseUri, vault))
                .add(Arguments.of(importer, null, vault))
                .add(Arguments.of(importer, baseUri, null))
                .build();
    }

    @ParameterizedTest
    @MethodSource("nullProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithNull(
            final VaultManagementController vaultManagementController,
            final KeyBackupRestoreController keyBackupRestoreController,
            final SecretBackupRestoreController secretBackupRestoreController,
            final CertificateBackupRestoreController certificateBackupRestoreController) {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () ->
                new VaultImportExportExecutor(vaultManagementController, keyBackupRestoreController,
                        secretBackupRestoreController, certificateBackupRestoreController));

        //then + exception
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testBackupVaultListShouldThrowExceptionWhenCalledWithNull() {
        //given
        final var vaultManagementController = mock(VaultManagementController.class);
        final var keyBackupRestoreController = mock(KeyBackupRestoreController.class);
        final var secretBackupRestoreController = mock(SecretBackupRestoreController.class);
        final var certificateBackupRestoreController = mock(CertificateBackupRestoreController.class);
        final var underTest = new VaultImportExportExecutor(vaultManagementController,
                keyBackupRestoreController, secretBackupRestoreController, certificateBackupRestoreController);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.backupVaultList(null));

        //then + exception
    }

    @ParameterizedTest
    @MethodSource("restoreNullProvider")
    void testBackupVaultListShouldThrowExceptionWhenCalledWithNull(
            final VaultImporter vaultImporter, final URI baseUri, final VaultModel vault) {
        //given
        final var vaultManagementController = mock(VaultManagementController.class);
        final var keyBackupRestoreController = mock(KeyBackupRestoreController.class);
        final var secretBackupRestoreController = mock(SecretBackupRestoreController.class);
        final var certificateBackupRestoreController = mock(CertificateBackupRestoreController.class);
        final var underTest = new VaultImportExportExecutor(vaultManagementController,
                keyBackupRestoreController, secretBackupRestoreController, certificateBackupRestoreController);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> underTest.restoreVault(vaultImporter, baseUri, vault));

        //then + exception
    }
}
