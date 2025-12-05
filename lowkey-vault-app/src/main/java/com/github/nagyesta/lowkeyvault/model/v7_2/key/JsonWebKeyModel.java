package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonWebKeyModel {

    @JsonProperty("kid")
    private String id;
    @JsonProperty("kty")
    private KeyType keyType;
    @JsonProperty("key_ops")
    private List<KeyOperation> keyOps = new ArrayList<>();
    @JsonProperty("n")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] n;
    @JsonProperty("e")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] e;
    @JsonProperty("d")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] d;
    @JsonProperty("dp")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] dp;
    @JsonProperty("dq")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] dq;
    @JsonProperty("qi")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] qi;
    @JsonProperty("p")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] p;
    @JsonProperty("q")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] q;
    @JsonProperty("k")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] k;
    @JsonProperty("key_hsm")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] keyHsm;
    @JsonProperty("crv")
    private KeyCurveName curveName;
    @JsonProperty("x")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] x;
    @JsonProperty("y")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] y;

}
