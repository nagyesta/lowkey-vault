package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.mapper.common.VaultFakeToVaultModelConverter;
import com.github.nagyesta.lowkeyvault.model.management.VaultModel;
import com.github.nagyesta.lowkeyvault.service.vault.VaultFake;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

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
        final VaultFake vaultFake = vaultService.create(model.getBaseUri(), model.getRecoveryLevel(), model.getRecoverableDays());
        return ResponseEntity.ok(vaultFakeToVaultModelConverter.convert(vaultFake));
    }

    @GetMapping
    public ResponseEntity<List<VaultModel>> listVaults() {
        final List<VaultModel> vaultFake = vaultService.list().stream()
                .map(vaultFakeToVaultModelConverter::convertNonNull)
                .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(vaultFake);
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<VaultModel>> listDeletedVaults() {
        final List<VaultModel> vaultFake = vaultService.listDeleted().stream()
                .map(vaultFakeToVaultModelConverter::convertNonNull)
                .collect(Collectors.toUnmodifiableList());
        return ResponseEntity.ok(vaultFake);
    }

    @DeleteMapping
    public ResponseEntity<Boolean> deleteVault(@RequestParam final URI baseUri) {
        return ResponseEntity.ok(vaultService.delete(baseUri));
    }

    @PutMapping("/recover")
    public ResponseEntity<VaultModel> recoverVault(@RequestParam final URI baseUri) {
        vaultService.recover(baseUri);
        final VaultFake fake = vaultService.findByUri(baseUri);
        return ResponseEntity.ok(vaultFakeToVaultModelConverter.convert(fake));
    }
}
