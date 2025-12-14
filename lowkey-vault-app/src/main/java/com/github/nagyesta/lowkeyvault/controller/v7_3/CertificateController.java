package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonCertificateController;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.*;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.KeyVaultItemListModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.*;
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
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3_AND_LATER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("certificateControllerV73")
@SuppressWarnings("java:S110")
public class CertificateController extends CommonCertificateController {

    public CertificateController(
            final VaultService vaultService,
            final CertificateEntityToV73ModelConverter modelConverter,
            final CertificateEntityToV73CertificateItemModelConverter itemConverter,
            final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter,
            final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter,
            final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter) {
        super(vaultService, modelConverter, itemConverter, pendingOperationConverter, issuancePolicyConverter, lifetimeActionConverter);
    }

    @Override
    @PostMapping(
            value = "/certificates/{certificateName}/create",
            version = V_7_3_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultPendingCertificateModel> create(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final CreateCertificateRequest request) {
        return super.create(certificateName, baseUri, apiVersion, request);
    }

    @Override
    @PostMapping(
            value = "/certificates/{certificateName}/import",
            version = V_7_3_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> importCertificate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final CertificateImportRequest request) {
        return super.importCertificate(certificateName, baseUri, apiVersion, request);
    }

    @Override
    @GetMapping(
            value = "/certificates/{certificateName}",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> get(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.get(certificateName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(
            value = "/certificates/{certificateName}/{certificateVersion}",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> getWithVersion(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.getWithVersion(certificateName, certificateVersion, baseUri, apiVersion);
    }

    @Override
    @DeleteMapping(
            value = "/certificates/{certificateName}",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultCertificateModel> delete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.delete(certificateName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(
            value = {"/deletedcertificates/{certificateName}", "/deletedCertificates/{certificateName}"},
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<DeletedKeyVaultCertificateModel> getDeletedCertificate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.getDeletedCertificate(certificateName, baseUri, apiVersion);
    }

    @Override
    @PostMapping(
            value = {"/deletedcertificates/{certificateName}/recover", "/deletedCertificates/{certificateName}/recover"},
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> recoverDeletedCertificate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.recoverDeletedCertificate(certificateName, baseUri, apiVersion);
    }

    @Override
    @DeleteMapping(
            value = {"/deletedcertificates/{certificateName}", "/deletedCertificates/{certificateName}"},
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> purgeDeleted(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.purgeDeleted(certificateName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(
            value = "/certificates/{certificateName}/versions",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> versions(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken) {
        return super.versions(certificateName, baseUri, apiVersion, maxResults, skipToken);
    }

    @Override
    @GetMapping(
            value = "/certificates",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<KeyVaultCertificateItemModel>> listCertificates(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken,
            @RequestParam(name = INCLUDE_PENDING_PARAM, required = false, defaultValue = TRUE) final boolean includePending) {
        return super.listCertificates(baseUri, apiVersion, maxResults, skipToken, includePending);
    }

    @Override
    @GetMapping(
            value = {"/deletedcertificates", "/deletedCertificates"},
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultItemListModel<DeletedKeyVaultCertificateItemModel>> listDeletedCertificates(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @RequestParam(name = MAX_RESULTS_PARAM, required = false, defaultValue = DEFAULT_MAX) final int maxResults,
            @RequestParam(name = SKIP_TOKEN_PARAM, required = false, defaultValue = SKIP_ZERO) final int skipToken,
            @RequestParam(name = INCLUDE_PENDING_PARAM, required = false, defaultValue = TRUE) final boolean includePending) {
        return super.listDeletedCertificates(baseUri, apiVersion, maxResults, skipToken, includePending);
    }

    @Override
    @PatchMapping(
            value = "/certificates/{certificateName}/{certificateVersion}",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> updateCertificateProperties(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String certificateVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final UpdateCertificateRequest request) {
        return super.updateCertificateProperties(certificateName, certificateVersion, baseUri, apiVersion, request);
    }

    @Override
    @GetMapping(value = "/certificates/{certificateName}/pending",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultPendingCertificateModel> pendingCreate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.pendingCreate(certificateName, baseUri, apiVersion);
    }

    @Override
    @DeleteMapping(value = "/certificates/{certificateName}/pending",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultPendingCertificateModel> pendingDelete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.pendingDelete(certificateName, baseUri, apiVersion);
    }

    @Override
    @GetMapping(value = "/certificates/{certificateName}/policy",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.getPolicy(certificateName, baseUri, apiVersion);
    }

    @Override
    @PatchMapping(value = "/certificates/{certificateName}/policy",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificatePolicyModel> updatePolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final CertificatePolicyModel request) {
        return super.updatePolicy(certificateName, baseUri, apiVersion, request);
    }

}
