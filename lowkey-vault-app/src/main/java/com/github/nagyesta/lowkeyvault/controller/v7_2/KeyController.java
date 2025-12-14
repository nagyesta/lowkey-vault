package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.*;
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
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2_AND_LATER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("keyControllerV72")
@SuppressWarnings("java:S110")
public class KeyController extends CommonKeyController {

    public KeyController(
            final VaultService vaultService,
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72KeyItemModelConverter itemConverter) {
        super(vaultService, modelConverter, itemConverter);
    }

    @Override
    @PostMapping(value = "/keys/{keyName}/create",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> create(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final CreateKeyRequest request) {
        return super.create(keyName, baseUri, apiVersion, request);
    }

    @Override
    @PutMapping(value = "/keys/{keyName}",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> importKey(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final ImportKeyRequest request) {
        return super.importKey(keyName, baseUri, apiVersion, request);
    }

    @Override
    @DeleteMapping(value = "/keys/{keyName}",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> delete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.delete(keyName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(value = "/keys/{keyName}/versions",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.versions(keyName, baseUri, apiVersion, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/keys",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> listKeys(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listKeys(baseUri, apiVersion, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = {"/deletedkeys", "/deletedKeys"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultKeyItemModel>> listDeletedKeys(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listDeletedKeys(baseUri, apiVersion, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/keys/{keyName}",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.get(keyName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(value = "/keys/{keyName}/{keyVersion}",
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.getWithVersion(keyName, keyVersion, baseUri, apiVersion);
    }

    @Override
    @PatchMapping(value = "/keys/{keyName}/{keyVersion}",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> updateVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final UpdateKeyRequest request) {
        return super.updateVersion(keyName, keyVersion, baseUri, apiVersion, request);
    }

    @Override
    @GetMapping(value = {"/deletedkeys/{keyName}", "/deletedKeys/{keyName}"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultKeyModel> getDeletedKey(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.getDeletedKey(keyName, baseUri, apiVersion);
    }

    @Override
    @PostMapping(value = {"/deletedkeys/{keyName}/recover", "/deletedKeys/{keyName}/recover"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> recoverDeletedKey(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.recoverDeletedKey(keyName, baseUri, apiVersion);
    }

    @Override
    @DeleteMapping(value = {"/deletedkeys/{keyName}", "/deletedKeys/{keyName}"},
            version = V_7_2_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeDeleted(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.purgeDeleted(keyName, baseUri, apiVersion);
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/encrypt",
            "/keys/{keyName}/{keyVersion}/wrapkey", "/keys/{keyName}/{keyVersion}/wrapKey"},
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> encrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        return super.encrypt(keyName, keyVersion, baseUri, V_7_2, request);
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/decrypt",
            "/keys/{keyName}/{keyVersion}/unwrapkey", "/keys/{keyName}/{keyVersion}/unwrapKey"},
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> decrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        return super.decrypt(keyName, keyVersion, baseUri, V_7_2, request);
    }

    @PostMapping(value = "/keys/{keyName}/{keyVersion}/sign",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeySignResult> sign(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeySignParameters request) {
        return super.sign(keyName, keyVersion, baseUri, V_7_2, request);
    }

    @PostMapping(value = "/keys/{keyName}/{keyVersion}/verify",
            version = V_7_2_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVerifyResult> verify(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyVerifyParameters request) {
        return super.verify(keyName, keyVersion, baseUri, V_7_2, request);
    }
}
