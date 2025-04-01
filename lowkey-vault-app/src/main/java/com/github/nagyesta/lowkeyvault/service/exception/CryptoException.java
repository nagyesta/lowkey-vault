package com.github.nagyesta.lowkeyvault.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CryptoException
        extends RuntimeException {

    public CryptoException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
