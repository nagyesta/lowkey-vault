package com.github.nagyesta.lowkeyvault.context;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.management.LowkeyVaultManagementClient;
import com.github.nagyesta.lowkeyvault.http.management.VaultModel;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ManagementTestContext {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private ApacheHttpClientProvider provider;
    private LowkeyVaultManagementClient client;
    private Map<String, List<VaultModel>> vaultLists = new TreeMap<>();

    public ManagementTestContext(final ApacheHttpClientProvider provider) {
        this.provider = provider;
    }

    public ApacheHttpClientProvider getProvider() {
        return provider;
    }

    public void setProvider(final ApacheHttpClientProvider provider) {
        this.provider = provider;
    }

    public synchronized LowkeyVaultManagementClient getClient() {
        if (client == null) {
            client = providerToClient(getProvider());
        }
        return client;
    }

    public void setClient(final LowkeyVaultManagementClient client) {
        this.client = client;
    }

    protected LowkeyVaultManagementClient providerToClient(final ApacheHttpClientProvider provider) {
        return provider.getLowkeyVaultManagementClient(objectMapper);
    }

    public Map<String, List<VaultModel>> getVaultLists() {
        return vaultLists;
    }
}
