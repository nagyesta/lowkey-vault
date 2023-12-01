package com.github.nagyesta.lowkeyvault.controller.v7_4;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyCryptoController;
import com.github.nagyesta.lowkeyvault.mapper.common.registry.KeyConverterRegistry;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyOperationsResult;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeySignResult;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVerifyResult;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeySignParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyVerifyParameters;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.RandomBytesRequest;
import com.github.nagyesta.lowkeyvault.model.v7_3.key.RandomBytesResponse;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
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

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_4;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_4;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("KeyCryptoControllerV74")
public class KeyCryptoController extends CommonKeyCryptoController {

    @Autowired
    public KeyCryptoController(@NonNull final KeyConverterRegistry registry, @NonNull final VaultService vaultService) {
        super(registry, vaultService);
    }

    @Override
    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/encrypt", "/keys/{keyName}/{keyVersion}/encrypt/",
            "/keys/{keyName}/{keyVersion}/wrapkey", "/keys/{keyName}/{keyVersion}/wrapkey/"},
            params = API_VERSION_7_4,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> encrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        return super.encrypt(keyName, keyVersion, baseUri, request);
    }

    @Override
    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/decrypt", "/keys/{keyName}/{keyVersion}/decrypt/",
            "/keys/{keyName}/{keyVersion}/unwrapkey", "/keys/{keyName}/{keyVersion}/unwrapkey/"},
            params = API_VERSION_7_4,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> decrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        return super.decrypt(keyName, keyVersion, baseUri, request);
    }

    @Override
    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/sign", "/keys/{keyName}/{keyVersion}/sign/"},
            params = API_VERSION_7_4,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeySignResult> sign(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeySignParameters request) {
        return super.sign(keyName, keyVersion, baseUri, request);
    }

    @Override
    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/verify", "/keys/{keyName}/{keyVersion}/verify/"},
            params = API_VERSION_7_4,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVerifyResult> verify(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyVerifyParameters request) {
        return super.verify(keyName, keyVersion, baseUri, request);
    }

    @PostMapping(value = {"/rng", "/rng/"},
            params = API_VERSION_7_4,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<RandomBytesResponse> getRandomBytes(
            @Valid @RequestBody final RandomBytesRequest request) {
        log.info("Received request to generate {} random bytes using API version: {}", request.getCount(), apiVersion());

        final byte[] randomBytes = KeyGenUtil.generateRandomBytes(request.getCount());
        return ResponseEntity.ok(new RandomBytesResponse(randomBytes));
    }

    @Override
    protected String apiVersion() {
        return V_7_4;
    }
}
