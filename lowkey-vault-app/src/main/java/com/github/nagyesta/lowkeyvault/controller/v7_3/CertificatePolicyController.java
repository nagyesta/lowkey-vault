package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonCertificatePolicyController;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73IssuancePolicyModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.CertificateEntityToV73PendingCertificateOperationModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.certificate.LifetimeActionsPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.KeyVaultPendingCertificateModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
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

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_3;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("CertificatePolicyControllerV73")
public class CertificatePolicyController extends CommonCertificatePolicyController {
    @Autowired
    public CertificatePolicyController(
            @NonNull final CertificateEntityToV73PendingCertificateOperationModelConverter pendingOperationConverter,
            @NonNull final CertificateEntityToV73IssuancePolicyModelConverter issuancePolicyConverter,
            @NonNull final LifetimeActionsPolicyToV73ModelConverter lifetimeActionsConverter,
            @NonNull final VaultService vaultService) {
        super(pendingOperationConverter, issuancePolicyConverter, lifetimeActionsConverter, vaultService, VaultFake::certificateVaultFake);
    }

    @Override
    @GetMapping(value = "/certificates/{certificateName}/pending",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultPendingCertificateModel> pendingCreate(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.pendingCreate(certificateName, baseUri);
    }

    @Override
    @DeleteMapping(value = "/certificates/{certificateName}/pending",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultPendingCertificateModel> pendingDelete(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.pendingDelete(certificateName, baseUri);
    }

    @Override
    @GetMapping(value = "/certificates/{certificateName}/policy",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificatePolicyModel> getPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String certificateName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getPolicy(certificateName, baseUri);
    }

    @Override
    protected String apiVersion() {
        return V_7_3;
    }
}
