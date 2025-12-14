package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonCertificateBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73CertificateItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateLifetimeActionsPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3_AND_LATER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("certificateBackupRestoreControllerV73")
@SuppressWarnings("java:S110")
public class CertificateBackupRestoreController extends CommonCertificateBackupRestoreController {

    public CertificateBackupRestoreController(
            final VaultService vaultService,
            final CertificateEntityToV73ModelConverter modelConverter,
            final CertificateEntityToV73CertificateItemModelConverter itemConverter,
            final CertificateLifetimeActionsPolicyToV73ModelConverter lifetimeActionConverter,
            final CertificateEntityToV73BackupConverter backupConverter) {
        super(vaultService, modelConverter, itemConverter, backupConverter, lifetimeActionConverter);
    }

    @Override
    @PostMapping(value = "/certificates/{certificateName}/backup",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateBackupModel> backup(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        return super.backup(certificateName, baseUri, apiVersion);
    }

    @Override
    @PostMapping(value = "/certificates/restore",
            version = V_7_3_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> restore(
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final CertificateBackupModel certificateBackupModel) {
        return super.restore(baseUri, apiVersion, certificateBackupModel);
    }
}
