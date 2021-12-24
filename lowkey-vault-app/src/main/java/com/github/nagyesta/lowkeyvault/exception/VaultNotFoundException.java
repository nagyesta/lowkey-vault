package com.github.nagyesta.lowkeyvault.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.URI;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VaultNotFoundException extends RuntimeException {

    private final URI baseUri;

    public VaultNotFoundException(final URI baseUri) {
        this.baseUri = baseUri;
    }

    @Override
    public String getMessage() {
        return "Vault not found by base URI: " + baseUri;
    }
}
