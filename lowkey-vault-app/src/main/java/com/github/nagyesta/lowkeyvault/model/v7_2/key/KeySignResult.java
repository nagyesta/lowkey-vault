package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.nagyesta.lowkeyvault.service.key.id.VersionedKeyEntityId;
import lombok.Data;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Base64;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeySignResult {

    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    @JsonProperty("kid")
    private URI id;
    @JsonProperty("value")
    private String value;

    public static KeySignResult forBytes(@org.springframework.lang.NonNull final VersionedKeyEntityId keyEntityId,
                                         @org.springframework.lang.NonNull final byte[] value,
                                         @org.springframework.lang.NonNull final URI vaultUri) {
        Assert.notNull(value, "Value must not be null.");
        return forString(keyEntityId, ENCODER.encodeToString(value), vaultUri);
    }

    private static KeySignResult forString(final VersionedKeyEntityId keyEntityId, final String value, final URI vaultUri) {
        final KeySignResult result = new KeySignResult();
        result.setId(keyEntityId.asUri(vaultUri));
        result.setValue(value);
        return result;
    }

}
