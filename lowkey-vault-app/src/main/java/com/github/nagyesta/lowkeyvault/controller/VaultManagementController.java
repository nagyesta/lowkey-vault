package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.mapper.common.VaultFakeToVaultModelConverter;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/management/vault")
public class VaultManagementController extends ErrorHandlingAwareController {

    private final VaultService vaultService;

    private final VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter;

    @Autowired
    public VaultManagementController(@NonNull final VaultService vaultService,
                                     @NonNull final VaultFakeToVaultModelConverter vaultFakeToVaultModelConverter) {
        this.vaultService = vaultService;
        this.vaultFakeToVaultModelConverter = vaultFakeToVaultModelConverter;
    }

    @PostMapping
    public ResponseEntity<VaultModel> createVault(@Valid @RequestBody final VaultModel model) {
        log.info("Received request to create vault with uri: {}, recovery level: {}, recoverable days: {}",
                model.getBaseUri(), model.getRecoveryLevel(), model.getRecoverableDays());
        final VaultFake vaultFake = vaultService.create(model.getBaseUri(), model.getRecoveryLevel(), model.getRecoverableDays());
        return ResponseEntity.ok(vaultFakeToVaultModelConverter.convert(vaultFake));
    }

    @GetMapping
    public ResponseEntity<List<VaultModel>> listVaults() {
        log.info("Received request to list vaults.");
        final List<VaultModel> vaultFake = vaultService.list().stream()
                .map(vaultFakeToVaultModelConverter::convertNonNull)
                .collect(Collectors.toUnmodifiableList());
        log.info("Returning {} vaults.", vaultFake.size());
        return ResponseEntity.ok(vaultFake);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<VaultModel>> listDeletedVaults() {
        log.info("Received request to list deleted vaults.");
        final List<VaultModel> vaultFake = vaultService.listDeleted().stream()
                .map(vaultFakeToVaultModelConverter::convertNonNull)
                .collect(Collectors.toUnmodifiableList());
        log.info("Returning {} vaults.", vaultFake.size());
        return ResponseEntity.ok(vaultFake);
    }

    @DeleteMapping
    public ResponseEntity<Boolean> deleteVault(@RequestParam final URI baseUri) {
        log.info("Received request to delete vault with uri: {}", baseUri);
        return ResponseEntity.ok(vaultService.delete(baseUri));
    }

    @PutMapping("/recover")
    public ResponseEntity<VaultModel> recoverVault(@RequestParam final URI baseUri) {
        log.info("Received request to recover deleted vault with uri: {}", baseUri);
        vaultService.recover(baseUri);
        final VaultFake fake = vaultService.findByUri(baseUri);
        return ResponseEntity.ok(vaultFakeToVaultModelConverter.convert(fake));
    }

    @DeleteMapping("/purge")
    public ResponseEntity<Boolean> purgeVault(@RequestParam final URI baseUri) {
        log.info("Received request to purge deleted vault with uri: {}", baseUri);
        return ResponseEntity.ok(vaultService.purge(baseUri));
    }

    @PutMapping(value = "/time", params = {"seconds"})
    public ResponseEntity<Void> timeShift(@Positive @RequestParam final int seconds) {
        log.info("Received request to shift time of ALL vaults by {} seconds.", seconds);
        vaultService.timeShift(seconds);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "/time", params = {"baseUri", "seconds"})
    public ResponseEntity<Void> timeShift(@RequestParam final URI baseUri, @Positive @RequestParam final int seconds) {
        log.info("Received request to shift time of vault with uri: {} by {} seconds.", baseUri, seconds);
        vaultService.findByUriIncludeDeleted(baseUri).timeShift(seconds);
        return ResponseEntity.noContent().build();
    }
}
