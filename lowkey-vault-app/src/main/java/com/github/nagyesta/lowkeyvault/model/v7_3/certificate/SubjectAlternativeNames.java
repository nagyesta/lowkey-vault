package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@Data
public class SubjectAlternativeNames {

    @JsonProperty("dns_names")
    private final Set<String> dnsNames;
    @JsonProperty("emails")
    private final Set<String> emails;
    @JsonProperty("upns")
    private final Set<String> upns;

    @JsonCreator
    public SubjectAlternativeNames(@JsonProperty("dns_names") final Set<String> dnsNames,
                                   @JsonProperty("emails") final Set<String> emails,
                                   @JsonProperty("upns") final Set<String> upns) {
        this.dnsNames = dnsNames;
        this.emails = emails;
        this.upns = upns;
    }
}
