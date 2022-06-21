package com.github.nagyesta.lowkeyvault.controller.v7_2;

import com.github.nagyesta.lowkeyvault.controller.common.CommonKeyBackupRestoreController;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72BackupConverter;
import com.github.nagyesta.lowkeyvault.mapper.v7_2.key.KeyEntityToV72ModelConverter;
import com.github.nagyesta.lowkeyvault.model.common.ApiConstants;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupList;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyBackupModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.KeyVaultKeyModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.JsonWebKeyImportRequest;
import com.github.nagyesta.lowkeyvault.service.vault.VaultService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.net.URI;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.API_VERSION_7_2;
import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.V_7_2;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@Validated
@Component("KeyBackupRestoreControllerV72")
public class KeyBackupRestoreController
        extends CommonKeyBackupRestoreController<KeyBackupListItem, KeyBackupList, KeyBackupModel, KeyEntityToV72BackupConverter> {

    @Autowired
    public KeyBackupRestoreController(final KeyEntityToV72ModelConverter modelConverter,
                                         final KeyEntityToV72BackupConverter backupConverter,
                                         final VaultService vaultService) {
        super(modelConverter, backupConverter, vaultService);
    }

    @Override
    @PostMapping(value = "/keys/{keyName}/backup",
            params = API_VERSION_7_2,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyBackupModel> backup(@PathVariable @Valid @Pattern(regexp = NAME_PATTERN) final String keyName,
                                                 @RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri) {
        return super.backup(keyName, baseUri);
    }

    @Override
    @PostMapping(value = "/keys/restore",
            params = API_VERSION_7_2,
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<KeyVaultKeyModel> restore(@RequestAttribute(name = ApiConstants.REQUEST_BASE_URI) final URI baseUri,
                                                    @Valid @RequestBody final KeyBackupModel keyBackupModel) {
        return super.restore(baseUri, keyBackupModel);
    }

    @Override
    protected JsonWebKeyImportRequest getKeyMaterial(final KeyBackupListItem entityVersion) {
        return entityVersion.getKeyMaterial();
    }

    @Override
    protected KeyBackupList getBackupList() {
        return new KeyBackupList();
    }

    @Override
    protected KeyBackupModel getBackupModel() {
        return new KeyBackupModel();
    }

    @Override
    protected String apiVersion() {
        return V_7_2;
    }
}
