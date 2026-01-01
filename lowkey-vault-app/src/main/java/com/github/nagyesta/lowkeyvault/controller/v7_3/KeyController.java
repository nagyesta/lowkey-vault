package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyToV73ModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_3.key.KeyRotationPolicyV73ModelToEntityConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.KeyRotationPolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.RandomBytesRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.RandomBytesResponse;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.validator.Update;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Objects;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_3_AND_LATER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("keyControllerV73")
@SuppressWarnings("java:S110")
public class KeyController extends CommonKeyController {

    private final KeyRotationPolicyToV73ModelConverter rotationPolicyModelConverter;
    private final KeyRotationPolicyV73ModelToEntityConverter rotationPolicyEntityConverter;

    public KeyController(
            final VaultService vaultService,
            final KeyEntityToV72ModelConverter modelConverter,
            final KeyEntityToV72KeyItemModelConverter itemConverter,
            final KeyRotationPolicyToV73ModelConverter rotationPolicyModelConverter,
            final KeyRotationPolicyV73ModelToEntityConverter rotationPolicyEntityConverter) {
        super(vaultService, modelConverter, itemConverter);
        this.rotationPolicyModelConverter = rotationPolicyModelConverter;
        this.rotationPolicyEntityConverter = rotationPolicyEntityConverter;
    }

    @PostMapping(value = "/keys/{keyName}/rotate",
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> rotateKey(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        log.info("Received request to {} rotate key: {} using API version: {}",
                baseUri, keyName, apiVersion);
        final var rotatedKeyId = getVaultByUri(baseUri).rotateKey(entityId(baseUri, keyName));
        final var keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, rotatedKeyId.version());
        return ResponseEntity.ok(convertDetails(keyVaultKeyEntity, baseUri));
    }

    @PostMapping(value = "/rng",
            version = V_7_3_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RandomBytesResponse> getRandomBytes(
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @Valid @RequestBody final RandomBytesRequest request) {
        log.info("Received request to generate {} random bytes using API version: {}", request.getCount(), apiVersion);
        final var randomBytes = KeyGenUtil.generateRandomBytes(request.getCount());
        return ResponseEntity.ok(new RandomBytesResponse(randomBytes));
    }

    @GetMapping(value = {"/keys/{keyName}/rotationpolicy", "/keys/{keyName}/rotationPolicy"},
            version = V_7_3_AND_LATER,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyRotationPolicyModel> getRotationPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion) {
        log.info("Received request to {} get rotation policy: {} using API version: {}",
                baseUri, keyName, apiVersion);
        return getRotationPolicyResponseEntity(getVaultByUri(baseUri), entityId(baseUri, keyName), baseUri);
    }

    @PutMapping(value = {"/keys/{keyName}/rotationpolicy", "/keys/{keyName}/rotationPolicy"},
            version = V_7_3_AND_LATER,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyRotationPolicyModel> updateRotationPolicy(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @RequestParam(name = ApiConstants.API_VERSION_NAME) final String apiVersion,
            @NotNull @Valid @Validated(Update.class) @RequestBody final KeyRotationPolicyModel request) {
        log.info("Received request to {} update rotation policy: {} using API version: {}",
                baseUri, keyName, apiVersion);
        final var keyEntityId = entityId(baseUri, keyName);
        request.setKeyEntityId(keyEntityId);
        final var rotationPolicy = Objects.requireNonNull(rotationPolicyEntityConverter.convert(request));
        final var keyVaultFake = getVaultByUri(baseUri);
        keyVaultFake.setRotationPolicy(rotationPolicy);
        return getRotationPolicyResponseEntity(keyVaultFake, keyEntityId, baseUri);
    }

    private ResponseEntity<KeyRotationPolicyModel> getRotationPolicyResponseEntity(
            final KeyVaultFake keyVaultFake,
            final KeyEntityId keyEntityId,
            final URI baseUri) {
        final var policy = keyVaultFake.rotationPolicy(keyEntityId);
        return ResponseEntity.ok(rotationPolicyModelConverter.convert(policy, baseUri));
    }
}
