package com.github.nagyesta.lowkeyvault;

import java.net.URI;

import static com.github.nagyesta.lowkeyvault.TestConstants.*;

@SuppressWarnings("checkstyle:JavadocVariable")
public final class TestConstantsUri {

    private TestConstantsUri() {
        throw new IllegalCallerException("Utility.");
    }

    //<editor-fold defaultstate="collapsed" desc="URIs">
    public static final URI HTTPS_LOCALHOST = URI.create(HTTPS + LOCALHOST);
    public static final URI HTTPS_LOCALHOST_8443 = URI.create(HTTPS_LOCALHOST + PORT_8443);
    public static final URI HTTPS_LOCALHOST_80 = URI.create(HTTPS_LOCALHOST + PORT_80);
    public static final URI HTTPS_LOOP_BACK_IP = URI.create(HTTPS + LOOP_BACK_IP);
    public static final URI HTTPS_LOOP_BACK_IP_8443 = URI.create(HTTPS_LOOP_BACK_IP + PORT_8443);
    public static final URI HTTPS_LOOP_BACK_IP_80 = URI.create(HTTPS_LOOP_BACK_IP + PORT_80);
    public static final URI HTTPS_LOWKEY_VAULT = URI.create(HTTPS + LOWKEY_VAULT);
    public static final URI HTTPS_LOWKEY_VAULT_8443 = URI.create(HTTPS_LOWKEY_VAULT + PORT_8443);
    public static final URI HTTPS_LOWKEY_VAULT_80 = URI.create(HTTPS_LOWKEY_VAULT + PORT_80);
    public static final URI HTTPS_DEFAULT_LOWKEY_VAULT = URI.create(HTTPS + DEFAULT_SUB + LOWKEY_VAULT);
    public static final URI HTTPS_DEFAULT_LOWKEY_VAULT_8443 = URI.create(HTTPS_DEFAULT_LOWKEY_VAULT + PORT_8443);
    public static final URI HTTPS_DEFAULT_LOWKEY_VAULT_80 = URI.create(HTTPS_DEFAULT_LOWKEY_VAULT + PORT_80);
    //</editor-fold>
}
