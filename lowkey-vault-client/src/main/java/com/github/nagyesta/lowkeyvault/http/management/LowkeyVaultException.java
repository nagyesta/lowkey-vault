package com.github.nagyesta.lowkeyvault.http.management;

public class LowkeyVaultException extends RuntimeException {

    public LowkeyVaultException(final String message) {
        super(message);
    }

    public LowkeyVaultException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
