package com.github.nagyesta.lowkeyvault.controller.v7_4;

import com.github.nagyesta.lowkeyvault.controller.common.CommonCertificateBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.CertificateConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.common.backup.CertificateBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultCertificateModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_4;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_4;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@DependsOn({"certificateBackupConverter", "certificateModelConverter"})
@Component("CertificateBackupRestoreControllerV74")
public class CertificateBackupRestoreController extends CommonCertificateBackupRestoreController {

    @Autowired
    public CertificateBackupRestoreController(
            @NonNull final CertificateConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    @PostMapping(value = {"/certificates/{certificateName}/backup", "/certificates/{certificateName}/backup/"},
            params = API_VERSION_7_4,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateBackupModel> backup(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
                                                         @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.backup(certificateName, baseUri);
    }

    @Override
    @PostMapping(value = {"/certificates/restore", "/certificates/restore/"},
            params = API_VERSION_7_4,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultCertificateModel> restore(@RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                            @Valid @RequestBody final CertificateBackupModel certificateBackupModel) {
        return super.restore(baseUri, certificateBackupModel);
    }

    @Override
    protected String apiVersion() {
        return V_7_4;
    }
}
