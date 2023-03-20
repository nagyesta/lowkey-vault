package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.SecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.model.common.ErrorModel;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupListModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
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

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping(value = "/management/vault",
        consumes = MimeTypeUtils.APPLICATION_JSON_VALUE,
        produces = MimeTypeUtils.APPLICATION_JSON_VALUE)
public class VaultBackupManagementController extends ErrorHandlingAwareController implements InitializingBean {

    private final VaultImporter vaultImporter;
    private final VaultService vaultService;
    private final VaultManagementController vaultManagementController;
    private final KeyBackupRestoreController keyBackupRestoreController;
    private final SecretBackupRestoreController secretBackupRestoreController;

    @Autowired
    public VaultBackupManagementController(@NonNull final VaultImporter vaultImporter,
                                           @NonNull final VaultService vaultService,
                                           @NonNull final VaultManagementController vaultManagementController,
                                           @NonNull final KeyBackupRestoreController keyBackupRestoreController,
                                           @NonNull final SecretBackupRestoreController secretBackupRestoreController) {
        this.vaultImporter = vaultImporter;
        this.vaultService = vaultService;
        this.vaultManagementController = vaultManagementController;
        this.keyBackupRestoreController = keyBackupRestoreController;
        this.secretBackupRestoreController = secretBackupRestoreController;
    }

    @Override
    public void afterPropertiesSet() {
        vaultImporter.getVaults().forEach((baseUri, vault) -> {
            vaultManagementController.createVault(vault);
            vaultImporter.getKeys().getOrDefault(baseUri, Collections.emptyList())
                    .forEach(key -> keyBackupRestoreController.restore(baseUri, key));
            vaultImporter.getSecrets().getOrDefault(baseUri, Collections.emptyList())
                    .forEach(secret -> secretBackupRestoreController.restore(baseUri, secret));
        });
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
    @GetMapping("/export")
    public ResponseEntity<VaultBackupListModel> export() {
        log.info("Received request to export active vaults.");
        final List<VaultBackupModel> backupModels = Optional.ofNullable(vaultManagementController.listVaults())
                .map(ResponseEntity::getBody)
                .orElse(Collections.emptyList()).stream()
                .map(this::backupVault)
                .collect(Collectors.toList());
        final VaultBackupListModel vaultBackupListModel = new VaultBackupListModel();
        vaultBackupListModel.setVaults(backupModels);
        log.info("Export completed.");
        return ResponseEntity.ok(vaultBackupListModel);
    }

    private Map<String, KeyBackupList> mapKeys(final URI baseUri) {
        return vaultService.findByUri(baseUri)
                .keyVaultFake().getEntities()
                .listLatestEntities().stream()
                .map(ReadOnlyKeyVaultKeyEntity::getId)
                .map(VersionedKeyEntityId::id)
                .collect(Collectors.toMap(Function.identity(), name -> backupKey(baseUri, name)));
    }

    private KeyBackupList backupKey(final URI baseUri, final String name) {
        return Optional.ofNullable(keyBackupRestoreController.backup(name, baseUri))
                .map(ResponseEntity::getBody)
                .map(KeyBackupModel::getValue)
                .orElseThrow(() -> new NotFoundException("Key not found: " + name));
    }

    private Map<String, SecretBackupList> mapSecrets(final URI baseUri) {
        return vaultService.findByUri(baseUri)
                .secretVaultFake().getEntities()
                .listLatestEntities().stream()
                .map(ReadOnlyKeyVaultSecretEntity::getId)
                .map(VersionedSecretEntityId::id)
                .collect(Collectors.toMap(Function.identity(), name -> backupSecret(baseUri, name)));
    }

    private SecretBackupList backupSecret(final URI baseUri, final String name) {
        return Optional.ofNullable(secretBackupRestoreController.backup(name, baseUri))
                .map(ResponseEntity::getBody)
                .map(SecretBackupModel::getValue)
                .orElseThrow(() -> new NotFoundException("Secret not found: " + name));
    }

    private VaultBackupModel backupVault(final VaultModel vaultModel) {
        final VaultBackupModel backupModel = new VaultBackupModel();
        backupModel.setAttributes(vaultModel);
        backupModel.setKeys(mapKeys(vaultModel.getBaseUri()));
        backupModel.setSecrets(mapSecrets(vaultModel.getBaseUri()));
        return backupModel;
    }
}
