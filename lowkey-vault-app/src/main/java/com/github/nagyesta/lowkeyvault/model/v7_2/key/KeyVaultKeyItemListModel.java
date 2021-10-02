package com.github.nagyesta.lowkeyvault.model.v7_2.key;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyVaultKeyItemListModel {
    @JsonProperty("nextLink")
    private String nextLink;

    @JsonProperty("value")
    private List<KeyVaultKeyItemModel> value;

    public KeyVaultKeyItemListModel(@NonNull final List<KeyVaultKeyItemModel> value,
                                    @Nullable final URI nextLinkUri) {
        Assert.notEmpty(value, "Value cannot be empty.");
        this.value = List.copyOf(value);
        this.nextLink = Optional.ofNullable(nextLinkUri).map(URI::toString).orElse(null);
    }
}
