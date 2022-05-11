package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.controller.common.CommonSecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.SecretEntityToV72BackupConverter;
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

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_2;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("SecretBackupRestoreControllerV72")
public class SecretBackupRestoreController extends CommonSecretBackupRestoreController {

    @Autowired
    protected SecretBackupRestoreController(
            @NonNull final SecretEntityToV72ModelConverter modelConverter,
            @NonNull final SecretEntityToV72BackupConverter backupConverter,
            @NonNull final VaultService vaultService) {
        super(modelConverter, backupConverter, vaultService);
    }

    @Override
    @PostMapping(value = "/secrets/{secretName}/backup",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SecretBackupModel> backup(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
                                                    @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.backup(secretName, baseUri);
    }

    @Override
    @PostMapping(value = "/secrets/restore",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> restore(@RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                       @Valid @RequestBody final SecretBackupModel secretBackupModel) {
        return super.restore(baseUri, secretBackupModel);
    }

    @Override
    protected String apiVersion() {
        return V_7_2;
    }
}
