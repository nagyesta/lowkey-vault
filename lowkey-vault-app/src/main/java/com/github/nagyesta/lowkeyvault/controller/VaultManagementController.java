package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.mapper.common.VaultFakeToVaultModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ErrorModel;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.github.nagyesta.lowkeyvault.openapi.Examples.*;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(value = "/management/vault", produces = APPLICATION_JSON_VALUE)
public class VaultManagementController extends ErrorHandlingAwareController {

    private final VaultService vaultService;

    private final VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter;

    @Autowired
    public VaultManagementController(@NonNull final VaultService vaultService,
                                     @NonNull final VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter) {
        this.vaultService = vaultService;
        this.vaultFakeToVaultModelConverter = vaultFakeToVaultModelConverter;
    }

    @Operation(
            summary = "Create a vault",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VaultModel.class))),
                    @ApiResponse(responseCode = "404", description = "Vault not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "400", description = "Validation Failure",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))},
            requestBody = @RequestBody(
                    content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VaultModel.class))))
    @PostMapping(value = {"", "/"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<VaultModel> createVault(@Valid @org.springframework.web.bind.annotation.RequestBody final VaultModel model) {
        log.info("Received request to create vault with uri: {}, recovery level: {}, recoverable days: {}",
                model.getBaseUri(), model.getRecoveryLevel(), model.getRecoverableDays());
        final VaultFake vaultFake = vaultService
                .create(model.getBaseUri(), model.getRecoveryLevel(), model.getRecoverableDays(), model.getAliases());
        return ResponseEntity.ok(vaultFakeToVaultModelConverter.convert(vaultFake));
    }

    @Operation(
            summary = "List active vaults",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed (result in response body)",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = VaultModel.class)))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))})
    @GetMapping(value = {"", "/"}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VaultModel>> listVaults() {
        log.info("Received request to list vaults.");
        final List<VaultModel> vaultFake = vaultService.list().stream()
                .map(vaultFakeToVaultModelConverter::convertNonNull)
                .toList();
        log.info("Returning {} vaults.", vaultFake.size());
        return ResponseEntity.ok(vaultFake);
    }

    @Operation(
            summary = "List deleted vaults",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed (result in response body)",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = VaultModel.class)))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))})
    @GetMapping(value = {"/deleted", "/deleted/"}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<VaultModel>> listDeletedVaults() {
        log.info("Received request to list deleted vaults.");
        final List<VaultModel> vaultFake = vaultService.listDeleted().stream()
                .map(vaultFakeToVaultModelConverter::convertNonNull)
                .toList();
        log.info("Returning {} vaults.", vaultFake.size());
        return ResponseEntity.ok(vaultFake);
    }

    @Operation(
            summary = "Delete a vault",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed (result in response body)",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class,
                                    description = "True if the operation changed something."))),
                    @ApiResponse(responseCode = "404", description = "Vault not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "400", description = "Validation Failure",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))},
            parameters = {
                    @Parameter(name = "baseUri",
                            example = BASE_URI, description = "The base URI of the vault we want delete.", required = true)})
    @DeleteMapping(value = {"", "/"}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> deleteVault(@RequestParam final URI baseUri) {
        log.info("Received request to delete vault with uri: {}", baseUri);
        return ResponseEntity.ok(vaultService.delete(baseUri));
    }

    @Operation(
            summary = "Recover a deleted vault",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VaultModel.class))),
                    @ApiResponse(responseCode = "404", description = "Vault not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "400", description = "Validation Failure",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))},
            parameters = {
                    @Parameter(name = "baseUri",
                            example = BASE_URI, description = "The base URI of the vault we want to recover.", required = true)},
            requestBody = @RequestBody(content = @Content(mediaType = APPLICATION_JSON_VALUE)))
    @PutMapping(value = {"/recover", "/recover/"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<VaultModel> recoverVault(@RequestParam final URI baseUri) {
        log.info("Received request to recover deleted vault with uri: {}", baseUri);
        vaultService.recover(baseUri);
        final VaultFake fake = vaultService.findByUri(baseUri);
        return ResponseEntity.ok(vaultFakeToVaultModelConverter.convert(fake));
    }

    @Operation(
            summary = "Purge a deleted vault",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed (result in response body)",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = Boolean.class,
                                    description = "True if the operation changed something."))),
                    @ApiResponse(responseCode = "404", description = "Vault not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "400", description = "Validation Failure",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))},
            parameters = {
                    @Parameter(name = "baseUri",
                            example = BASE_URI, description = "The base URI of the vault we want to purge.", required = true)})
    @DeleteMapping(value = {"/purge", "/purge/"}, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> purgeVault(@RequestParam final URI baseUri) {
        log.info("Received request to purge deleted vault with uri: {}", baseUri);
        return ResponseEntity.ok(vaultService.purge(baseUri));
    }

    @Operation(
            summary = "Update aliases of a vault",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Operation completed (result in response body)",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = VaultModel.class))),
                    @ApiResponse(responseCode = "404", description = "Vault not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "400", description = "Validation Failure",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))},
            parameters = {
                    @Parameter(name = "baseUri", example = BASE_URI,
                            description = "The base URI of the vault we want to update.", required = true),
                    @Parameter(name = "add", example = ALIAS1,
                            description = "The base URI we want to add to the aliases of the vault."),
                    @Parameter(name = "remove", example = ALIAS2,
                            description = "The base URI we want to remove from the aliases of the vault.")},
            requestBody = @RequestBody(content = @Content(mediaType = APPLICATION_JSON_VALUE)))
    @PatchMapping(value = {"/alias", "/alias/"}, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<VaultModel> aliasUpdate(@RequestParam final URI baseUri,
                                                  @RequestParam(required = false) final URI add,
                                                  @RequestParam(required = false) final URI remove) {
        log.info("Received request to update alias of vault with uri: {}", baseUri);
        final VaultFake fake = vaultService.updateAlias(baseUri, add, remove);
        return ResponseEntity.ok(vaultFakeToVaultModelConverter.convert(fake));
    }

    @Operation(
            summary = "Time shift for ALL vaults",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successful Operation",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "400", description = "Validation Failure",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))},
            parameters = {
                    @Parameter(name = "seconds", example = ONE, description = "The number of seconds we want to shift.", required = true),
                    @Parameter(name = "regenerateCertificates", example = FALSE,
                            description = "Whether we allow regeneration of certificates to let their validity match the new time-frame.")},
            requestBody = @RequestBody(content = @Content(mediaType = APPLICATION_JSON_VALUE)))
    @PutMapping(value = {"/time/all", "/time/all/"}, params = {"seconds"},
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> timeShiftAll(
            @RequestParam final int seconds,
            @RequestParam(required = false, defaultValue = "false") final boolean regenerateCertificates) {
        log.info("Received request to shift time of ALL vaults by {} seconds, regenerate certificates: {}.",
                seconds, regenerateCertificates);
        vaultService.timeShift(seconds, regenerateCertificates);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Time shift for a single vault",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successful Operation",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Vault not found",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "400", description = "Validation Failure",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class))),
                    @ApiResponse(responseCode = "500", description = "Internal error",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorModel.class)))},
            parameters = {
                    @Parameter(name = "seconds", example = ONE, description = "The number of seconds we want to shift.", required = true),
                    @Parameter(name = "baseUri",
                            example = BASE_URI, description = "The base URI of the vault we want to shift.", required = true),
                    @Parameter(name = "regenerateCertificates", example = FALSE,
                            description = "Whether we allow regeneration of certificates to let their validity match the new time-frame.")},
            requestBody = @RequestBody(content = @Content(mediaType = APPLICATION_JSON_VALUE)))
    @PutMapping(value = {"/time", "/time/"}, params = {"baseUri", "seconds"},
            consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> timeShiftSingle(
            @RequestParam final URI baseUri, @RequestParam final int seconds,
            @RequestParam(required = false, defaultValue = "false") final boolean regenerateCertificates) {
        log.info("Received request to shift time of vault with uri: {}, by {} seconds, regenerate certificates: {}.",
                baseUri, seconds, regenerateCertificates);
        vaultService.findByUriIncludeDeleted(baseUri).timeShift(seconds, regenerateCertificates);
        return ResponseEntity.noContent().build();
    }
}
