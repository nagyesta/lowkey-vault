package com.github.nagyesta.lowkeyvault.management;

import com.github.nagyesta.lowkeyvault.controller.VaultManagementController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.CertificateBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.SecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.model.common.backup.*;
import com.github.nagyesta.lowkeyvault.model.management.VaultBackupModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.service.certificate.ReadOnlyKeyVaultCertificateEntity;
import com.github.nagyesta.lowkeyvault.service.certificate.id.VersionedCertificateEntityId;
import com.github.nagyesta.lowkeyvault.service.exception.NotFoundException;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import com.github.nagyesta.lowkeyvault.template.backup.VaultImporter;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class VaultImportExportExecutor {

    private final VaultManagementController vaultManagementController;
    private final KeyBackupRestoreController keyBackupRestoreController;
    private final SecretBackupRestoreController secretBackupRestoreController;
    private final CertificateBackupRestoreController certificateBackupRestoreController;

    @Autowired
    public VaultImportExportExecutor(@NonNull final VaultManagementController vaultManagementController,
                                     @NonNull final KeyBackupRestoreController keyBackupRestoreController,
                                     @NonNull final SecretBackupRestoreController secretBackupRestoreController,
                                     @NonNull final CertificateBackupRestoreController certificateBackupRestoreController) {
        this.vaultManagementController = vaultManagementController;
        this.keyBackupRestoreController = keyBackupRestoreController;
        this.secretBackupRestoreController = secretBackupRestoreController;
        this.certificateBackupRestoreController = certificateBackupRestoreController;
    }

    public void restoreVault(@NonNull final VaultImporter vaultImporter, @NonNull final URI baseUri, @NonNull final VaultModel vault) {
        vaultManagementController.createVault(vault);
        vaultImporter.getKeys().getOrDefault(baseUri, Collections.emptyList())
                .forEach(key -> keyBackupRestoreController.restore(baseUri, key));
        vaultImporter.getSecrets().getOrDefault(baseUri, Collections.emptyList())
                .forEach(secret -> secretBackupRestoreController.restore(baseUri, secret));
        vaultImporter.getCertificates().getOrDefault(baseUri, Collections.emptyList())
                .forEach(certificate -> certificateBackupRestoreController.restore(baseUri, certificate));
    }

    public List<VaultBackupModel> backupVaultList(@NonNull final VaultService vaultService) {
        return Optional.ofNullable(vaultManagementController.listVaults())
                .map(ResponseEntity::getBody)
                .orElse(Collections.emptyList()).stream()
                .map(vaultModel -> backupVault(vaultService, vaultModel))
                .collect(Collectors.toList());
    }

    private KeyBackupList backupKey(final URI baseUri, final String name) {
        return Optional.ofNullable(keyBackupRestoreController.backup(name, baseUri))
                .map(ResponseEntity::getBody)
                .map(KeyBackupModel::getValue)
                .orElseThrow(() -> new NotFoundException("Key not found: " + name));
    }

    private SecretBackupList backupSecret(final URI baseUri, final String name) {
        return Optional.ofNullable(secretBackupRestoreController.backup(name, baseUri))
                .map(ResponseEntity::getBody)
                .map(SecretBackupModel::getValue)
                .orElseThrow(() -> new NotFoundException("Secret not found: " + name));
    }

    private CertificateBackupList backupCertificate(final URI baseUri, final String name) {
        return Optional.ofNullable(certificateBackupRestoreController.backup(name, baseUri))
                .map(ResponseEntity::getBody)
                .map(CertificateBackupModel::getValue)
                .orElseThrow(() -> new NotFoundException("Certificate not found: " + name));
    }

    private VaultBackupModel backupVault(final VaultService vaultService, final VaultModel vaultModel) {
        final var vaultFake = vaultService.findByUri(vaultModel.getBaseUri());
        final var backupModel = new VaultBackupModel();
        backupModel.setAttributes(vaultModel);
        backupModel.setKeys(mapKeys(vaultFake));
        backupModel.setSecrets(mapSecrets(vaultFake));
        backupModel.setCertificates(mapCertificates(vaultFake));
        return backupModel;
    }

    private Map<String, KeyBackupList> mapKeys(final VaultFake vaultFake) {
        return vaultFake.keyVaultFake().getEntities()
                //exclude managed entities as certificates will take care of those
                .listLatestNonManagedEntities().stream()
                .map(ReadOnlyKeyVaultKeyEntity::getId)
                .map(VersionedKeyEntityId::id)
                .collect(Collectors.toMap(Function.identity(), name -> backupKey(vaultFake.baseUri(), name)));
    }

    private Map<String, SecretBackupList> mapSecrets(final VaultFake vaultFake) {
        return vaultFake.secretVaultFake().getEntities()
                //exclude managed entities as certificates will take care of those
                .listLatestNonManagedEntities().stream()
                .map(ReadOnlyKeyVaultSecretEntity::getId)
                .map(VersionedSecretEntityId::id)
                .collect(Collectors.toMap(Function.identity(), name -> backupSecret(vaultFake.baseUri(), name)));
    }

    private Map<String, CertificateBackupList> mapCertificates(final VaultFake vaultFake) {
        return vaultFake.certificateVaultFake().getEntities()
                .listLatestEntities().stream()
                .map(ReadOnlyKeyVaultCertificateEntity::getId)
                .map(VersionedCertificateEntityId::id)
                .collect(Collectors.toMap(Function.identity(), name -> backupCertificate(vaultFake.baseUri(), name)));
    }
}
