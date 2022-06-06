package com.github.nagyesta.lowkeyvault.model.v7_2.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Base class of backup models.
 *
 * @param <P>   The type of the properties model.
 * @param <BLI> The type of the backup list items.
 * @param <BL>  The wrapper type of the backup list.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseBackupModel<P extends BasePropertiesModel, BLI extends BaseBackupListItem<P>, BL extends BackupListContainer<BLI>> {

    @Valid
    @NotNull
    private BL value;
}
