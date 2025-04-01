package com.github.nagyesta.lowkeyvault.model.v7_3.certificate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public record SubjectAlternativeNames(
        @JsonProperty("dns_names") Set<String> dnsNames,
        @JsonProperty("emails") Set<String> emails,
        @JsonProperty("upns") Set<String> upns) {

    @JsonCreator
    @SuppressWarnings({"java:S1186", "java:S6207"}) //default JSON creator
    public SubjectAlternativeNames {
    }
}
