package com.github.nagyesta.lowkeyvault.controller.v7_5;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyPolicyController;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
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

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_5;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_5;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("keyPolicyControllerV75")
@SuppressWarnings("java:S110")
public class KeyPolicyController
        extends CommonKeyPolicyController {

    public KeyPolicyController(
            @NonNull final KeyConverterRegistry registry,
            @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    @GetMapping(value = {"/keys/{keyName}/rotationpolicy", "/keys/{keyName}/rotationPolicy"},
            params = API_VERSION_7_5,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyRotationPolicyModel> getRotationPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.getRotationPolicy(keyName, baseUri);
    }

    @Override
    @PutMapping(value = {"/keys/{keyName}/rotationpolicy", "/keys/{keyName}/rotationPolicy"},
            params = API_VERSION_7_5,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyRotationPolicyModel> updateRotationPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @NonNull @Valid @Validated(Update.class) @RequestBody final KeyRotationPolicyModel request) {
        return super.updateRotationPolicy(keyName, baseUri, request);
    }

    @Override
    protected String apiVersion() {
        return V_7_5;
    }

}
