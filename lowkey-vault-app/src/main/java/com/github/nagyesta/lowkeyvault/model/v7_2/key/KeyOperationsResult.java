package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import lombok.Data;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.net.URI;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyOperationsResult {

    @JsonProperty("kid")
    private URI id;
    @JsonProperty("aad")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] additionalAuthData;
    @JsonProperty("iv")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] initializationVector;
    @JsonProperty("tag")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte @Nullable [] authenticationTag;
    @JsonProperty("value")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] value;

    public static KeyOperationsResult forBytes(
            final VersionedKeyEntityId keyEntityId,
            final byte[] value,
            final KeyOperationsParameters input,
            final URI vaultUri) {
        final var result = new KeyOperationsResult();
        result.setId(keyEntityId.asUri(vaultUri));
        result.setValue(value);
        result.setInitializationVector(input.getInitializationVector());
        result.setAdditionalAuthData(input.getAdditionalAuthData());
        result.setAuthenticationTag(input.getAuthenticationTag());
        return result;
    }

}
