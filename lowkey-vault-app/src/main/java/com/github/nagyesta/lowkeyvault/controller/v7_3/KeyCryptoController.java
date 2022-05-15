package com.github.nagyesta.lowkeyvault.controller.v7_3;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyCryptoController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
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
@Component("KeyCryptoControllerV73")
public class KeyCryptoController extends CommonKeyCryptoController {

    @Autowired
    public KeyCryptoController(@NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
                               @NonNull final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
                               @NonNull final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
                               @NonNull final VaultService vaultService) {
        super(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService);
    }

    @Override
    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/encrypt", "/keys/{keyName}/{keyVersion}/wrap"},
            params = API_VERSION_7_3,
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
    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/decrypt", "/keys/{keyName}/{keyVersion}/unwrap"},
            params = API_VERSION_7_3,
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
    @PostMapping(value = "/keys/{keyName}/{keyVersion}/sign",
            params = API_VERSION_7_3,
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
    @PostMapping(value = "/keys/{keyName}/{keyVersion}/verify",
            params = API_VERSION_7_3,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVerifyResult> verify(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyVerifyParameters request) {
        return super.verify(keyName, keyVersion, baseUri, request);
    }

    @PostMapping(value = "/rng",
            params = API_VERSION_7_3,
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
        return V_7_3;
    }
}
