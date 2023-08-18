package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.management.VaultImportExportExecutor;
import com.github.nagyesta.lowkeyvault.model.common.ErrorModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/management/vault",
        consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public class VaultBackupManagementController extends ErrorHandlingAwareController implements InitializingBean {

    private final VaultImporter vaultImporter;
    private final VaultService vaultService;
    private final VaultImportExportExecutor vaultImportExportExecutor;

    @Autowired
    public VaultBackupManagementController(@NonNull final VaultImporter vaultImporter,
                                           @NonNull final VaultService vaultService,
                                           @NonNull final VaultImportExportExecutor vaultImportExportExecutor) {
        this.vaultImporter = vaultImporter;
        this.vaultService = vaultService;
        this.vaultImportExportExecutor = vaultImportExportExecutor;
    }

    @Override
    public void afterPropertiesSet() {
        vaultImporter.getVaults().forEach((baseUri, vault) -> vaultImportExportExecutor.restoreVault(vaultImporter, baseUri, vault));
    }

    @Operation(
            summary = "Exports active vaults",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed (result in response body)",
                            content = @Content(
                                    mediaType = MimeTypeUtils.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = VaultBackupListModel.class)))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(
                                    mediaType = MimeTypeUtils.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorModel.class)
                            ))},
            requestBody = @RequestBody(content = @Content(mediaType = MimeTypeUtils.APPLICATION_JSON_VALUE)))
    @GetMapping(value = {"/export", "/export/"})
    public ResponseEntity<VaultBackupListModel> export() {
        log.info("Received request to export active vaults.");
        final List<VaultBackupModel> backupModels = vaultImportExportExecutor.backupVaultList(vaultService);
        final VaultBackupListModel vaultBackupListModel = new VaultBackupListModel();
        vaultBackupListModel.setVaults(backupModels);
        log.info("Export completed.");
        return ResponseEntity.ok(vaultBackupListModel);
    }
}
