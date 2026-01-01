package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.common.DeletedModel;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.EpochSecondsSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeletedKeyVaultCertificateModel extends KeyVaultCertificateModel implements DeletedModel {

    @JsonProperty("recoveryId")
    private String recoveryId;
    @JsonProperty("deletedDate")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime deletedDate;
    @JsonProperty("scheduledPurgeDate")
    @JsonSerialize(using = EpochSecondsSerializer.class)
    @JsonDeserialize(using = EpochSecondsDeserializer.class)
    private OffsetDateTime scheduledPurgeDate;

}
