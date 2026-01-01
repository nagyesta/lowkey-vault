package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.backup.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2_AND_LATER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("keyBackupRestoreControllerV72")
@SuppressWarnings("java:S110")
public class KeyBackupRestoreController extends CommonKeyBackupRestoreController {

    public KeyBackupRestoreController(
            final VaultService vaultService,
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72KeyItemModelConverter itemConverter,
            final KeyEntityToV72BackupConverter backupConverter) {
        super(vaultService, modelConverter, itemConverter, backupConverter);
    }

    @Override
    @PostMapping(value = "/keys/{keyName}/backup",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyBackupModel> backup(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.backup(keyName, baseUri, apiVersion);
    }

    @Override
    @PostMapping(value = "/keys/restore",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> restore(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final KeyBackupModel keyBackupModel) {
        return super.restore(baseUri, apiVersion, keyBackupModel);
    }
}
