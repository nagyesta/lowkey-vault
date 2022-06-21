package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_3;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("KeyBackupRestoreControllerV73")
public class KeyBackupRestoreController
        extends CommonKeyBackupRestoreController<KeyBackupListItem, KeyBackupList, KeyBackupModel, KeyEntityToV72BackupConverter> {

    private final KeyRotationPolicyToV73ModelConverter keyRotationPolicyToV73ModelConverter;
    private final KeyRotationPolicyV73ModelToEntityConverter rotationV73ModelToEntityConverter;

    @Autowired
    public KeyBackupRestoreController(@NonNull final KeyEntityToV72ModelConverter modelConverter,
                                         @NonNull final KeyEntityToV72BackupConverter backupConverter,
                                         @NonNull final VaultService vaultService,
                                         @NonNull final KeyRotationPolicyToV73ModelConverter keyRotationPolicyToV73ModelConverter,
                                         @NonNull final KeyRotationPolicyV73ModelToEntityConverter rotationV73ModelToEntityConverter) {
        super(modelConverter, backupConverter, vaultService);
        this.keyRotationPolicyToV73ModelConverter = keyRotationPolicyToV73ModelConverter;
        this.rotationV73ModelToEntityConverter = rotationV73ModelToEntityConverter;
    }

    @Override
    @PostMapping(value = "/keys/{keyName}/backup",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyBackupModel> backup(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                 @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.backup(keyName, baseUri);
    }

    @Override
    @PostMapping(value = "/keys/restore",
            params = API_VERSION_7_3,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> restore(@RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                    @Valid @RequestBody final KeyBackupModel keyBackupModel) {
        return super.restore(baseUri, keyBackupModel);
    }

    @Override
    protected KeyBackupModel backupEntity(final KeyEntityId entityId) {
        final KeyBackupModel keyBackupModel = super.backupEntity(entityId);
        final KeyBackupList value = keyBackupModel.getValue();
        final ReadOnlyRotationPolicy rotationPolicy = getVaultByUri(entityId.vault()).rotationPolicy(entityId);
        value.setKeyRotationPolicy(keyRotationPolicyToV73ModelConverter.convert(rotationPolicy));
        return keyBackupModel;
    }

    @Override
    protected KeyVaultKeyModel restoreEntity(final KeyBackupModel backupModel) {
        final KeyVaultKeyModel keyVaultKeyModel = super.restoreEntity(backupModel);
        final URI baseUri = getSingleBaseUri(backupModel);
        final String entityName = getSingleEntityName(backupModel);
        final KeyEntityId keyEntityId = entityId(baseUri, entityName);
        final KeyVaultFake vaultByUri = getVaultByUri(baseUri);
        final KeyRotationPolicyModel keyRotationPolicy = backupModel.getValue().getKeyRotationPolicy();
        Optional.ofNullable(keyRotationPolicy)
                .map(r -> rotationV73ModelToEntityConverter.convert(keyEntityId, r))
                .ifPresent(vaultByUri::setRotationPolicy);
        return keyVaultKeyModel;
    }

    @Override
    protected String apiVersion() {
        return V_7_3;
    }

    @Override
    protected JsonWebKeyImportRequest getKeyMaterial(final KeyBackupListItem entityVersion) {
        return entityVersion.getKeyMaterial();
    }

    @Override
    protected KeyBackupList getBackupList() {
        return new KeyBackupList();
    }

    @Override
    protected KeyBackupModel getBackupModel() {
        return new KeyBackupModel();
    }
}
