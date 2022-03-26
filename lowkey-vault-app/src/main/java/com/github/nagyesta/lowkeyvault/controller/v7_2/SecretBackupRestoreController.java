package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.*;
import com.github.nagyesta.lowkeyvault.service.secret.ReadOnlyKeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.secret.SecretVaultFake;
import com.github.nagyesta.lowkeyvault.service.secret.id.SecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.id.VersionedSecretEntityId;
import com.github.nagyesta.lowkeyvault.service.secret.impl.KeyVaultSecretEntity;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
public class SecretBackupRestoreController extends BaseBackupRestoreController<SecretEntityId, VersionedSecretEntityId,
        ReadOnlyKeyVaultSecretEntity, KeyVaultSecretModel, DeletedKeyVaultSecretModel, SecretPropertiesModel, SecretBackupListItem,
        SecretBackupList, SecretBackupModel, SecretEntityToV72BackupConverter, SecretEntityToV72ModelConverter, SecretVaultFake> {

    @Autowired
    protected SecretBackupRestoreController(
            @NonNull final SecretEntityToV72ModelConverter modelConverter,
            @NonNull final SecretEntityToV72BackupConverter backupConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, backupConverter, vaultService, VaultFake::secretVaultFake);
    }

    @PostMapping(value = "/secrets/{secretName}/backup",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SecretBackupModel> backup(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
                                                    @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} backup secret: {} using API version: {}",
                baseUri.toString(), secretName, V_7_2);
        return ResponseEntity.ok(backupEntity(entityId(baseUri, secretName)));
    }

    @PostMapping(value = "/secrets/restore",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> restore(@RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                       @Valid @RequestBody final SecretBackupModel secretBackupModel) {
        log.info("Received request to {} restore secret: {} using API version: {}",
                baseUri.toString(), secretBackupModel.getValue().get(0).getId(), V_7_2);
        return ResponseEntity.ok(restoreEntity(secretBackupModel));
    }

    @Override
    protected void restoreVersion(@NonNull final SecretVaultFake vault,
                                  @NonNull final VersionedSecretEntityId versionedEntityId,
                                  @NonNull final SecretBackupListItem entityVersion) {
        vault.createSecretVersion(versionedEntityId, entityVersion.getValue(), entityVersion.getContentType());
        final KeyVaultSecretEntity entity = vault.getEntities().getEntity(versionedEntityId, KeyVaultSecretEntity.class);
        updateCommonFields(entityVersion, entity);
    }

    @Override
    protected SecretBackupList getBackupList() {
        return new SecretBackupList();
    }

    @Override
    protected SecretBackupModel getBackupModel() {
        return new SecretBackupModel();
    }

    @Override
    protected VersionedSecretEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedSecretEntityId(baseUri, name, version);
    }

    @Override
    protected SecretEntityId entityId(final URI baseUri, final String name) {
        return new SecretEntityId(baseUri, name);
    }
}
