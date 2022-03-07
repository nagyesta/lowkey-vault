package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72KeyVersionItemModelConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.*;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeySignParameters;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyVerifyParameters;
import com.github.nagyesta.lowkeyvault.service.key.KeyVaultFake;
import com.github.nagyesta.lowkeyvault.service.key.ReadOnlyKeyVaultKeyEntity;
import com.github.nagyesta.lowkeyvault.service.key.id.KeyEntityId;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
public class KeyCryptoController extends BaseController<KeyEntityId, VersionedKeyEntityId, ReadOnlyKeyVaultKeyEntity,
        KeyVaultKeyModel, DeletedKeyVaultKeyModel, KeyVaultKeyItemModel, DeletedKeyVaultKeyItemModel,
        KeyEntityToV72ModelConverter, KeyEntityToV72KeyItemModelConverter, KeyEntityToV72KeyVersionItemModelConverter,
        KeyVaultFake> {

    @Autowired
    public KeyCryptoController(@NonNull final KeyEntityToV72ModelConverter keyEntityToV72ModelConverter,
                               @NonNull final KeyEntityToV72KeyItemModelConverter keyEntityToV72KeyItemModelConverter,
                               @NonNull final KeyEntityToV72KeyVersionItemModelConverter keyEntityToV72KeyVersionItemModelConverter,
                               @NonNull final VaultService vaultService) {
        super(keyEntityToV72ModelConverter, keyEntityToV72KeyItemModelConverter,
                keyEntityToV72KeyVersionItemModelConverter, vaultService, VaultFake::keyVaultFake);
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/encrypt", "/keys/{keyName}/{keyVersion}/wrap"},
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> encrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        log.info("Received request to {} encrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] encrypted = keyVaultKeyEntity.encryptBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), encrypted, request));
    }

    @PostMapping(value = {"/keys/{keyName}/{keyVersion}/decrypt", "/keys/{keyName}/{keyVersion}/unwrap"},
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyOperationsResult> decrypt(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyOperationsParameters request) {
        log.info("Received request to {} decrypt using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] decrypted = keyVaultKeyEntity.decryptToBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getInitializationVector());
        return ResponseEntity.ok(KeyOperationsResult.forBytes(keyVaultKeyEntity.getId(), decrypted, request));
    }

    @PostMapping(value = "/keys/{keyName}/{keyVersion}/sign",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeySignResult> sign(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeySignParameters request) {
        log.info("Received request to {} sign using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final byte[] signature = keyVaultKeyEntity.signBytes(request.getValueAsBase64DecodedBytes(), request.getAlgorithm());
        return ResponseEntity.ok(KeySignResult.forBytes(keyVaultKeyEntity.getId(), signature));
    }

    @PostMapping(value = "/keys/{keyName}/{keyVersion}/verify",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVerifyResult> verify(
            @PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
            @PathVariable @Valid @Pattern(regexp = VERSION_NAME_PATTERN) final String keyVersion,
            @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
            @Valid @RequestBody final KeyVerifyParameters request) {
        log.info("Received request to {} verify using key: {} with version: {} using API version: {}",
                baseUri.toString(), keyName, keyVersion, V_7_2);

        final ReadOnlyKeyVaultKeyEntity keyVaultKeyEntity = getEntityByNameAndVersion(baseUri, keyName, keyVersion);
        final boolean result = keyVaultKeyEntity.verifySignedBytes(request.getDigestAsBase64DecodedBytes(), request.getAlgorithm(),
                request.getValueAsBase64DecodedBytes());
        return ResponseEntity.ok(new KeyVerifyResult(result));
    }

    @Override
    protected VersionedKeyEntityId versionedEntityId(final URI baseUri, final String name, final String version) {
        return new VersionedKeyEntityId(baseUri, name, version);
    }

    @Override
    protected KeyEntityId entityId(final URI baseUri, final String name) {
        return new KeyEntityId(baseUri, name);
    }

}
