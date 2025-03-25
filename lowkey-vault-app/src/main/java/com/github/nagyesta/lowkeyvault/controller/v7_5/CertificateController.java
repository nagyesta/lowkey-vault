package com.github.nagyesta.lowkeyvault.controller.v7_5;

import com.github.nagyesta.lowkeyvault.controller.common.CommonCertificateController;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@Component("CertificateControllerV75")
public class CertificateController extends CommonCertificateController {
    @Autowired
    public CertificateController(@NonNull final CertificateConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    @PostMapping(
            value = "/certificates/{certificateName}/create",
            params = API_VERSION_7_5,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultPendingCertificateModel> create(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final CreateCertificateRequest request) {
        return super.create(certificateName, baseUri, request);
    }

    @Override
    @PostMapping(
            value = "/certificates/{certificateName}/import",
            params = API_VERSION_7_5,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> importCertificate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final CertificateImportRequest request) {
        return super.importCertificate(certificateName, baseUri, request);
    }

    @Override
    @GetMapping(
            value = "/certificates/{certificateName}",
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.get(certificateName, baseUri);
    }

    @Override
    @GetMapping(
            value = "/certificates/{certificateName}/{certificateVersion}",
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getWithVersion(certificateName, certificateVersion, baseUri);
    }

    @Override
    @DeleteMapping(
            value = "/certificates/{certificateName}",
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultCertificateModel> delete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.delete(certificateName, baseUri);
    }

    @Override
    @GetMapping(
            value = {"/deletedcertificates/{certificateName}", "/deletedCertificates/{certificateName}"},
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultCertificateModel> getDeletedCertificate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getDeletedCertificate(certificateName, baseUri);
    }

    @Override
    @PostMapping(
            value = {"/deletedcertificates/{certificateName}/recover", "/deletedCertificates/{certificateName}/recover"},
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> recoverDeletedCertificate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.recoverDeletedCertificate(certificateName, baseUri);
    }

    @Override
    @DeleteMapping(
            value = {"/deletedcertificates/{certificateName}", "/deletedCertificates/{certificateName}"},
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeDeleted(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.purgeDeleted(certificateName, baseUri);
    }

    @Override
    @GetMapping(
            value = "/certificates/{certificateName}/versions",
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.versions(certificateName, baseUri, maxResults, skipToken);
    }

    @Override
    @GetMapping(
            value = "/certificates",
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> listCertificates(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken,
            @RequestParam(name = INCLUDE_PENDING_PARAM, required = false, defaultValue = TRUE) final boolean includePending) {
        return super.listCertificates(baseUri, maxResults, skipToken, includePending);
    }

    @Override
    @GetMapping(
            value = {"/deletedcertificates", "/deletedCertificates"},
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel>> listDeletedCertificates(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken,
            @RequestParam(name = INCLUDE_PENDING_PARAM, required = false, defaultValue = TRUE) final boolean includePending) {
        return super.listDeletedCertificates(baseUri, maxResults, skipToken, includePending);
    }

    @Override
    @PatchMapping(
            value = "/certificates/{certificateName}/{certificateVersion}",
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> updateCertificateProperties(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final UpdateCertificateRequest request) {
        return super.updateCertificateProperties(certificateName, certificateVersion, baseUri, request);
    }

    @Override
    protected String apiVersion() {
        return V_7_5;
    }
}
