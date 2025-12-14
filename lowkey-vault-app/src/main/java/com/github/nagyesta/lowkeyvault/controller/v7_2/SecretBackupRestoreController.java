package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.controller.common.CommonSecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
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
@Component("secretBackupRestoreControllerV72")
@SuppressWarnings("java:S110") //the simplicity of the implementation outweighs the risk
public class SecretBackupRestoreController extends CommonSecretBackupRestoreController {

    public SecretBackupRestoreController(
            final VaultService vaultService,
            final SecretEntityToV72ModelConverter modelConverter,
            final SecretEntityToV72SecretItemModelConverter itemConverter,
            final SecretEntityToV72BackupConverter backupConverter) {
        super(vaultService, modelConverter, itemConverter, backupConverter);
    }

    @Override
    @PostMapping(value = "/secrets/{secretName}/backup",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SecretBackupModel> backup(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.backup(secretName, baseUri, apiVersion);
    }

    @Override
    @PostMapping(value = "/secrets/restore",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> restore(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final SecretBackupModel secretBackupModel) {
        return super.restore(baseUri, apiVersion, secretBackupModel);
    }

}
