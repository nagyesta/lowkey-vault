package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.DeletedKeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyItemModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.CreateKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.ImportKeyRequest;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.UpdateKeyRequest;
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
@Component("KeyControllerV72")
public class KeyController extends CommonKeyController {

    @Autowired
    public KeyController(@NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
                         @NonNull final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
                         @NonNull final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
                         @NonNull final VaultService vaultService) {
        super(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService);
    }

    @Override
    @PostMapping(value = "/keys/{keyName}/create",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> create(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final CreateKeyRequest request) {
        return super.create(keyName, baseUri, request);
    }

    @Override
    @PutMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> importKey(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final ImportKeyRequest request) {
        return super.importKey(keyName, baseUri, request);
    }

    @Override
    @DeleteMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> delete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.delete(keyName, baseUri);
    }

    @Override
    @GetMapping(value = "/keys/{keyName}/versions",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.versions(keyName, baseUri, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/keys",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultKeyItemModel>> listKeys(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listKeys(baseUri, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/deletedkeys",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultKeyItemModel>> listDeletedKeys(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.listDeletedKeys(baseUri, maxResults, skipToken);
    }

    @Override
    @GetMapping(value = "/keys/{keyName}",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.get(keyName, baseUri);
    }

    @Override
    @GetMapping(value = "/keys/{keyName}/{keyVersion}",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getWithVersion(keyName, keyVersion, baseUri);
    }

    @Override
    @PatchMapping(value = "/keys/{keyName}/{keyVersion}",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> updateVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @NonNull @Valid @RequestBody final UpdateKeyRequest request) {
        return super.updateVersion(keyName, keyVersion, baseUri, request);
    }

    @Override
    @GetMapping(value = "/deletedkeys/{keyName}",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultKeyModel> getDeletedKey(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getDeletedKey(keyName, baseUri);
    }

    @Override
    @PostMapping(value = "/deletedkeys/{keyName}/recover",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> recoverDeletedKey(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.recoverDeletedKey(keyName, baseUri);
    }

    @Override
    @DeleteMapping(value = "/deletedkeys/{keyName}",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeDeleted(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.purgeDeleted(keyName, baseUri);
    }

    @Override
    protected String apiVersion() {
        return V_7_2;
    }
}
