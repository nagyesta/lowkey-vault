package com.github.nagyesta.lowkeyvault.management;

import com.github.nagyesta.lowkeyvault.controller.VaultManagementController;
import com.github.nagyesta.lowkeyvault.controller.v7_2.SecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.CertificateBackupRestoreController;
import com.github.nagyesta.lowkeyvault.controller.v7_3.KeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
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
    public VaultImportExportExecutor(
            final VaultManagementController vaultManagementController,
            final KeyBackupRestoreController keyBackupRestoreController,
            final SecretBackupRestoreController secretBackupRestoreController,
            final CertificateBackupRestoreController certificateBackupRestoreController) {
        this.vaultManagementController = vaultManagementController;
        this.keyBackupRestoreController = keyBackupRestoreController;
        this.secretBackupRestoreController = secretBackupRestoreController;
        this.certificateBackupRestoreController = certificateBackupRestoreController;
    }

    public void restoreVault(
            final VaultImporter vaultImporter,
            final URI baseUri,
            final VaultModel vault) {
        vaultManagementController.createVault(vault);
        vaultImporter.getKeys().getOrDefault(baseUri, Collections.emptyList())
                .forEach(key -> keyBackupRestoreController.restore(baseUri, ApiConstants.LATEST, key));
        vaultImporter.getSecrets().getOrDefault(baseUri, Collections.emptyList())
                .forEach(secret -> secretBackupRestoreController.restore(baseUri, ApiConstants.LATEST, secret));
        vaultImporter.getCertificates().getOrDefault(baseUri, Collections.emptyList())
                .forEach(certificate -> certificateBackupRestoreController.restore(baseUri, ApiConstants.LATEST, certificate));
    }

    public List<VaultBackupModel> backupVaultList(final VaultService vaultService) {
        return Optional.of(vaultManagementController.listVaults())
                .map(ResponseEntity::getBody)
                .orElse(Collections.emptyList()).stream()
                .map(vaultModel -> backupVault(vaultService, vaultModel))
                .toList();
    }

    private KeyBackupList backupKey(
            final URI baseUri,
            final String name) {
        final var optionalList = Optional.of(keyBackupRestoreController.backup(name, baseUri, ApiConstants.LATEST))
                .map(ResponseEntity::getBody)
                .map(KeyBackupModel::getValue);
        if (optionalList.isEmpty()) {
            throw new NotFoundException("Key not found: " + name);
        }
        return optionalList.get();
    }

    private SecretBackupList backupSecret(
            final URI baseUri,
            final String name) {
        final var optionalList = Optional.of(secretBackupRestoreController.backup(name, baseUri, ApiConstants.LATEST))
                .map(ResponseEntity::getBody)
                .map(SecretBackupModel::getValue);
        if (optionalList.isEmpty()) {
            throw new NotFoundException("Secret not found: " + name);
        }
        return optionalList.get();
    }

    private CertificateBackupList backupCertificate(
            final URI baseUri,
            final String name) {
        final var optionalList = Optional.of(certificateBackupRestoreController.backup(name, baseUri, ApiConstants.LATEST))
                .map(ResponseEntity::getBody)
                .map(CertificateBackupModel::getValue);
        if (optionalList.isEmpty()) {
            throw new NotFoundException("Certificate not found: " + name);
        }
        return optionalList.get();
    }

    private VaultBackupModel backupVault(
            final VaultService vaultService,
            final VaultModel vaultModel) {
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
