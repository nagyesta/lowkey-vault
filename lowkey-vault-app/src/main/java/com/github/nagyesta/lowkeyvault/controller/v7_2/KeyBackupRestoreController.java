package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.impl.KeyVaultKeyEntity;
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
import java.util.Collections;
import java.util.Objects;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
public class KeyBackupRestoreController extends BaseBackupRestoreController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyPropertiesModel, KeyBackupListItem, KeyBackupList, KeyBackupModel,
        KeyEntityToV72BackupConverter, KeyEntityToV72ModelConverter, KeyVaultFake> {

    @Autowired
    protected KeyBackupRestoreController(
            @NonNull final KeyEntityToV72ModelConverter modelConverter,
            @NonNull final KeyEntityToV72BackupConverter backupConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, backupConverter, vaultService, VaultFake::keyVaultFake);
    }

    @PostMapping(value = "/keys/{keyName}/backup",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyBackupModel> backup(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                 @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} backup key: {} using API version: {}",
                baseUri.toString(), keyName, V_7_2);
        return ResponseEntity.ok(backupEntity(entityId(baseUri, keyName)));
    }

    @PostMapping(value = "/keys/restore",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> restore(@RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                    @Valid @RequestBody final KeyBackupModel keyBackupModel) {
        log.info("Received request to {} restore key: {} using API version: {}",
                baseUri.toString(), keyBackupModel.getValue().get(0).getId(), V_7_2);
        return ResponseEntity.ok(restoreEntity(keyBackupModel));
    }

    @Override
    protected void restoreVersion(@NonNull final KeyVaultFake vault,
                                  @NonNull final VersionedKeyEntityId versionedEntityId,
                                  @NonNull final KeyBackupListItem entityVersion) {
        vault.importKeyVersion(versionedEntityId, entityVersion.getKeyMaterial());
        final KeyVaultKeyEntity<?, ?> entity = vault.getEntities().getEntity(versionedEntityId, KeyVaultKeyEntity.class);
        entity.setOperations(Objects.requireNonNullElse(entityVersion.getKeyMaterial().getKeyOps(), Collections.emptyList()));
        updateCommonFields(entityVersion, entity);
    }

    @Override
    protected KeyBackupList getBackupList() {
        return new KeyBackupList();
    }

    @Override
    protected KeyBackupModel getBackupModel() {
        return new KeyBackupModel();
    }

    @Override
    protected VersionedKeyEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(final URI baseUri, final String name) {
        return new KeyEntityId(baseUri, name);
    }
}
