package com.github.nagyesta.lowkeyvault.controller.v7_5;

import com.github.nagyesta.lowkeyvault.controller.common.CommonSecretBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.backup.SecretBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_5;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_5;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@DependsOn("secretModelConverter")
@Component("secretBackupRestoreControllerV75")
@SuppressWarnings("java:S110")
public class SecretBackupRestoreController
        extends CommonSecretBackupRestoreController {

    public SecretBackupRestoreController(
            @NonNull final SecretConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    @PostMapping(value = "/secrets/{secretName}/backup",
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<SecretBackupModel> backup(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.backup(secretName, baseUri);
    }

    @Override
    @PostMapping(value = "/secrets/restore",
            params = API_VERSION_7_5,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> restore(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final SecretBackupModel secretBackupModel) {
        return super.restore(baseUri, secretBackupModel);
    }

    @Override
    protected String apiVersion() {
        return V_7_5;
    }
}
