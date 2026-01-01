package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.controller.common.CommonSecretController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.secret.SecretEntityToV72SecretItemModelConverter;
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
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.controller.common.PaginationContext.*;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2_AND_LATER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("secretControllerV72")
@SuppressWarnings("java:S110")
public class SecretController extends CommonSecretController {

    public SecretController(
            final VaultService vaultService,
            final SecretEntityToV72ModelConverter secretEntityToV72ModelConverter,
            final SecretEntityToV72SecretItemModelConverter secretEntityToV72SecretItemModelConverter) {
        super(vaultService, secretEntityToV72ModelConverter, secretEntityToV72SecretItemModelConverter);
    }

    @Override
    @PutMapping(value = "/secrets/{secretName}",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> create(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final CreateSecretRequest request) {
        return super.create(secretName, baseUri, apiVersion, request);
    }

    @Override
    @DeleteMapping(value = "/secrets/{secretName}",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultSecretModel> delete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.delete(secretName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(value = "/secrets/{secretName}/versions",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.versions(secretName, baseUri, apiVersion, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/secrets",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultSecretItemModel>> listSecrets(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listSecrets(baseUri, apiVersion, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = {"/deletedsecrets", "/deletedSecrets"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultSecretItemModel>> listDeletedSecrets(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listDeletedSecrets(baseUri, apiVersion, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/secrets/{secretName}",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.get(secretName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(value = "/secrets/{secretName}/{secretVersion}",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.getWithVersion(secretName, secretVersion, baseUri, apiVersion);
    }

    @Override
    @PatchMapping(value = "/secrets/{secretName}/{secretVersion}",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> updateVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String secretVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final UpdateSecretRequest request) {
        return super.updateVersion(secretName, secretVersion, baseUri, apiVersion, request);
    }

    @Override
    @GetMapping(value = {"/deletedsecrets/{secretName}", "/deletedSecrets/{secretName}"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> getDeletedSecret(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.getDeletedSecret(secretName, baseUri, apiVersion);
    }

    @Override
    @DeleteMapping(value = {"/deletedsecrets/{secretName}", "/deletedSecrets/{secretName}"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeDeleted(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.purgeDeleted(secretName, baseUri, apiVersion);
    }

    @Override
    @PostMapping(value = {"/deletedsecrets/{secretName}/recover", "/deletedSecrets/{secretName}/recover"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultSecretModel> recoverDeletedSecret(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String secretName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.recoverDeletedSecret(secretName, baseUri, apiVersion);
    }
}
