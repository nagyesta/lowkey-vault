package com.github.nagyesta.lowkeyvault.context;

import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;

public class TestContextConfig {

    /**
     * The host port mapped to the SSL port of the container.
     */
    public static final String CONTAINER_PORT = "8444";
    /**
     * The hostname we can use to access the container from the host.
     */
    public static final String CONTAINER_HOST = "localhost";
    /**
     * The authority we can use to access the container from the host.
     */
    public static final String CONTAINER_AUTHORITY = CONTAINER_HOST + ":" + CONTAINER_PORT;
    /**
     * Default base URI.
     */
    public static final String DEFAULT_CONTAINER_URL = "https://" + CONTAINER_HOST + ":" + CONTAINER_PORT;

    private final CertificateTestContext certificateTestContext;
    private final SecretTestContext secretTestContext;
    private final ManagementTestContext managementTestContext;
    private final KeyTestContext keyTestContext;

    public TestContextConfig() {
        final ApacheHttpClientProvider provider = new ApacheHttpClientProvider(DEFAULT_CONTAINER_URL);
        managementTestContext = new ManagementTestContext(provider);
        keyTestContext = new KeyTestContext(provider);
        secretTestContext = new SecretTestContext(provider);
        certificateTestContext = new CertificateTestContext(provider);
    }

    public KeyTestContext keyContext() {
        return keyTestContext;
    }

    public CertificateTestContext certificateContext() {
        return certificateTestContext;
    }

    public SecretTestContext secretContext() {
        return secretTestContext;
    }

    public ManagementTestContext managementContext() {
        return managementTestContext;
    }
}
