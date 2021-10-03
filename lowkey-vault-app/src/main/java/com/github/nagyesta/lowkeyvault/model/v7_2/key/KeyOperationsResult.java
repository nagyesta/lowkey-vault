package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Deserializer;
import com.github.nagyesta.lowkeyvault.model.json.util.Base64Serializer;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.KeyOperationsParameters;
import com.github.nagyesta.lowkeyvault.service.VersionedKeyEntityId;
import lombok.Data;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Base64;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyOperationsResult {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    @JsonProperty("kid")
    private URI id;
    @JsonProperty("aad")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] additionalAuthData;
    @JsonProperty("iv")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] initializationVector;
    @JsonProperty("tag")
    @JsonSerialize(using = Base64Serializer.class)
    @JsonDeserialize(using = Base64Deserializer.class)
    private byte[] authenticationTag;
    @JsonProperty("value")
    private String value;

    public static KeyOperationsResult forBytes(@org.springframework.lang.NonNull final VersionedKeyEntityId keyEntityId,
                                               @org.springframework.lang.NonNull final byte[] value,
                                               @org.springframework.lang.NonNull final KeyOperationsParameters input) {
        Assert.notNull(value, "Value must not be null.");
        return forString(keyEntityId, ENCODER.encodeToString(value), input);
    }

    public static KeyOperationsResult forString(@NonNull final VersionedKeyEntityId keyEntityId,
                                                @NonNull final String value,
                                                @NonNull final KeyOperationsParameters input) {
        final KeyOperationsResult result = new KeyOperationsResult();
        result.setId(keyEntityId.asUri());
        result.setValue(value);
        result.setInitializationVector(input.getInitializationVector());
        result.setAdditionalAuthData(input.getAdditionalAuthData());
        result.setAuthenticationTag(input.getAuthenticationTag());
        return result;
    }

}
