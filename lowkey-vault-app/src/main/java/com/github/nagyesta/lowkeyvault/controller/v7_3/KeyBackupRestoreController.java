package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_3;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@DependsOn({"keyModelConverter", "keyRotationPolicyEntityConverter"})
@Component("KeyBackupRestoreControllerV73")
public class KeyBackupRestoreController extends CommonKeyBackupRestoreController {

    @Autowired
    public KeyBackupRestoreController(@NonNull final KeyConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    @PostMapping(value = {"/keys/{keyName}/backup", "/keys/{keyName}/backup/"},
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyBackupModel>
    backup(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
           @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.backup(keyName, baseUri);
    }

    @Override
    @PostMapping(value = {"/keys/restore", "/keys/restore/"},
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
        value.setKeyRotationPolicy(registry().rotationPolicyModelConverter(apiVersion()).convert(rotationPolicy, entityId.vault()));
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
                .map(r -> {
                    r.setKeyEntityId(keyEntityId);
                    return r;
                })
                .map(r -> registry().rotationPolicyEntityConverter(apiVersion()).convert(r))
                .ifPresent(vaultByUri::setRotationPolicy);
        return keyVaultKeyModel;
    }

    @Override
    protected String apiVersion() {
        return V_7_3;
    }
}
