package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.BaseKeyController;
import com.github.nagyesta.lowkeyvault.mapper.common.AliasAwareConverter;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyRotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.RotationPolicy;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
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
@Component("KeyPolicyControllerV73")
public class KeyPolicyController extends BaseKeyController {

    public KeyPolicyController(@NonNull final KeyConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @GetMapping(value = "/keys/{keyName}/rotationpolicy",
            params = API_VERSION_7_3,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyRotationPolicyModel> getRotationPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        log.info("Received request to {} get rotation policy: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());
        return getRotationPolicyResponseEntity(getVaultByUri(baseUri), entityId(baseUri, keyName), baseUri);
    }

    @PutMapping(value = "/keys/{keyName}/rotationpolicy",
            params = API_VERSION_7_3,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyRotationPolicyModel> updateRotationPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @NonNull @Valid @Validated(Update.class) @RequestBody final KeyRotationPolicyModel request) {
        log.info("Received request to {} update rotation policy: {} using API version: {}",
                baseUri.toString(), keyName, apiVersion());
        final KeyEntityId keyEntityId = entityId(baseUri, keyName);
        request.setKeyEntityId(keyEntityId);
        final RotationPolicy rotationPolicy = registry().rotationPolicyEntityConverter(apiVersion()).convert(request);
        final KeyVaultFake keyVaultFake = getVaultByUri(baseUri);
        keyVaultFake.setRotationPolicy(rotationPolicy);
        return getRotationPolicyResponseEntity(keyVaultFake, keyEntityId, baseUri);
    }

    @Override
    protected String apiVersion() {
        return V_7_3;
    }

    private ResponseEntity<KeyRotationPolicyModel> getRotationPolicyResponseEntity(
            final KeyVaultFake keyVaultFake, final KeyEntityId keyEntityId, final URI baseUri) {
        final ReadOnlyRotationPolicy policy = keyVaultFake.rotationPolicy(keyEntityId);
        final AliasAwareConverter<ReadOnlyRotationPolicy, KeyRotationPolicyModel> converter = registry()
                .rotationPolicyModelConverter(apiVersion());
        return ResponseEntity.ok(converter.convert(policy, baseUri));
    }
}
