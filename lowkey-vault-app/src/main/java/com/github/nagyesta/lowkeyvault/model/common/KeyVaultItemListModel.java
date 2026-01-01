package com.github.nagyesta.lowkeyvault.model.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.jspecify.annotations.Nullable;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KeyVaultItemListModel<E> {

    @Nullable
    @JsonProperty("nextLink")
    private String nextLink;

    @JsonProperty("value")
    private List<E> value;

    public KeyVaultItemListModel(
            final List<E> value,
            @Nullable final URI nextLinkUri) {
        this.value = List.copyOf(value);
        this.nextLink = Optional.ofNullable(nextLinkUri).map(URI::toString).orElse(null);
    }
}
