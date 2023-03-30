package com.github.nagyesta.lowkeyvault.model.common.backup;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.controller.common.util.CertificateRequestMapperUtil;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertDeserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64CertSerializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.common.BaseBackupListItem;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePolicyModel;
import com.github.nagyesta.lowkeyvault.model.v7_3.certificate.CertificatePropertiesModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CertificateBackupListItem extends BaseBackupListItem<CertificatePropertiesModel> {

    @JsonProperty("pwd")
    private String password;

    @JsonProperty("keyVersion")
    private String keyVersion;

    @NotNull
    @JsonSerialize(using = Base64CertSerializer.class)
    @JsonDeserialize(using = Base64CertDeserializer.class)
    @JsonProperty("value")
    private byte[] certificate;

    @JsonProperty("policy")
    private CertificatePolicyModel policy;

    @JsonProperty("issuancePolicy")
    private CertificatePolicyModel issuancePolicy;

    @JsonIgnore
    public String getCertificateAsString() {
        return CertificateRequestMapperUtil.getCertificateAsString(certificate);
    }
}
