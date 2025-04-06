package com.github.nagyesta.lowkeyvault.model.v7_2.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.nagyesta.lowkeyvault.model.v7_2.BasePropertiesModel;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.BackupListContainer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Base class of backup models.
 *
 * @param <P>   The type of the properties model.
 * @param <BLI> The type of the backup list items.
 * @param <BL>  The wrapper type of the backup list.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("java:S119") //It is easier to ensure that the types are consistent this way
public class BaseBackupModel<P extends BasePropertiesModel, BLI extends BaseBackupListItem<P>, BL extends BackupListContainer<BLI>> {

    @Valid
    @NotNull
    private BL value;
}
