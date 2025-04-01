package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonSecretController;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.SecretConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.DeletedKeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.KeyVaultSecretModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.CreateSecretRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.secret.request.UpdateSecretRequest;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.controller.common.PaginationContext.*;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_3;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("secretControllerV73")
@SuppressWarnings("java:S110")
public class SecretController
        extends CommonSecretController {

    public SecretController(
            @NonNull final SecretConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    @PutMapping(value = "/secrets/{secretName}",
            params = API_VERSION_7_3,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> create(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final CreateSecretRequest request) {
        return super.create(secretName, baseUri, request);
    }

    @Override
    @DeleteMapping(value = "/secrets/{secretName}",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultSecretModel> delete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.delete(secretName, baseUri);
    }

    @Override
    @GetMapping(value = "/secrets/{secretName}/versions",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.versions(secretName, baseUri, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/secrets",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> listSecrets(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listSecrets(baseUri, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = {"/deletedsecrets", "/deletedSecrets"},
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultSecretItemModel>> listDeletedSecrets(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listDeletedSecrets(baseUri, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/secrets/{secretName}",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.get(secretName, baseUri);
    }

    @Override
    @GetMapping(value = "/secrets/{secretName}/{secretVersion}",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getWithVersion(secretName, secretVersion, baseUri);
    }

    @Override
    @PatchMapping(value = "/secrets/{secretName}/{secretVersion}",
            params = API_VERSION_7_3,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> updateVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @NonNull @Valid @RequestBody final UpdateSecretRequest request) {
        return super.updateVersion(secretName, secretVersion, baseUri, request);
    }

    @Override
    @GetMapping(value = {"/deletedsecrets/{secretName}", "/deletedSecrets/{secretName}"},
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> getDeletedSecret(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getDeletedSecret(secretName, baseUri);
    }

    @Override
    @DeleteMapping(value = {"/deletedsecrets/{secretName}", "/deletedSecrets/{secretName}"},
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeDeleted(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.purgeDeleted(secretName, baseUri);
    }

    @Override
    @PostMapping(value = {"/deletedsecrets/{secretName}/recover", "/deletedSecrets/{secretName}/recover"},
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> recoverDeletedSecret(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.recoverDeletedSecret(secretName, baseUri);
    }

    @Override
    protected String apiVersion() {
        return V_7_3;
    }
}
