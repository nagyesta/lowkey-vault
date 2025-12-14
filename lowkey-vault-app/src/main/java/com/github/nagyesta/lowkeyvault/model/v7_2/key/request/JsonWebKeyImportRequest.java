package com.github.nagyesta.lowkeyvault.model.v7_2.key.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyCurveName;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyOperation;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.validator.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

@Data
@ValidImportKey
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonWebKeyImportRequest {

    private static final int RSA_MODULUS_BYTES_TO_KEY_SIZE_BITS_MULTIPLIER = 64;
    @Nullable
    @Null(groups = {RsaKey.class, OctKey.class})
    @NotNull(groups = EcKey.class)
    @JsonProperty("crv")
    private KeyCurveName curveName;
    @Null(groups = OctKey.class)
    @NotNull(groups = {RsaKey.class, EcKey.class})
    @Size(min = 1, groups = {RsaKey.class, EcKey.class})
    @JsonProperty("d")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] d;
    @Null(groups = {EcKey.class, OctKey.class})
    @NotNull(groups = RsaKey.class)
    @Size(min = 1, groups = RsaKey.class)
    @JsonProperty("dp")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] dp;
    @Null(groups = {EcKey.class, OctKey.class})
    @NotNull(groups = RsaKey.class)
    @Size(min = 1, groups = RsaKey.class)
    @JsonProperty("dq")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] dq;
    @Null(groups = {EcKey.class, OctKey.class})
    @NotNull(groups = RsaKey.class)
    @Size(min = 1, groups = RsaKey.class)
    @JsonProperty("e")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] e;
    @Null(groups = {RsaKey.class, EcKey.class})
    @NotNull(groups = OctKey.class)
    @Size(min = 1, groups = OctKey.class)
    @JsonProperty("k")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] k;
    @JsonProperty("key_hsm")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] keyHsm;
    @JsonProperty("key_ops")
    private List<KeyOperation> keyOps = new ArrayList<>();
    @Nullable
    @JsonProperty("kid")
    private String id;
    @NotNull(groups = {BaseKey.class, RsaKey.class, EcKey.class, OctKey.class})
    @JsonProperty("kty")
    private KeyType keyType;
    @Null(groups = {EcKey.class, OctKey.class})
    @NotNull(groups = RsaKey.class)
    @Size(min = 1, groups = RsaKey.class)
    @JsonProperty("n")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte@Nullable [] n;
    @Null(groups = {EcKey.class, OctKey.class})
    @NotNull(groups = RsaKey.class)
    @Size(min = 1, groups = RsaKey.class)
    @JsonProperty("p")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte@Nullable [] p;
    @Null(groups = {EcKey.class, OctKey.class})
    @NotNull(groups = RsaKey.class)
    @Size(min = 1, groups = RsaKey.class)
    @JsonProperty("q")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte@Nullable [] q;
    @Null(groups = {EcKey.class, OctKey.class})
    @NotNull(groups = RsaKey.class)
    @Size(min = 1, groups = RsaKey.class)
    @JsonProperty("qi")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte@Nullable [] qi;
    @Null(groups = {RsaKey.class, OctKey.class})
    @NotNull(groups = EcKey.class)
    @Size(min = 1, groups = EcKey.class)
    @JsonProperty("x")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte@Nullable [] x;
    @Null(groups = {RsaKey.class, OctKey.class})
    @NotNull(groups = EcKey.class)
    @Size(min = 1, groups = EcKey.class)
    @JsonProperty("y")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte@Nullable [] y;

}
