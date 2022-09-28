package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

@Testcontainers
class LowkeyVaultContainerJupiterTest extends AbstractLowkeyVaultContainerTest {

    private static final String VAULT_NAME = "jupiter";
    public static final String ALIAS = "lowkey-vault.local";

    @Container
    private final LowkeyVaultContainer underTest = lowkeyVault(DockerImageName
            .parse(getCurrentLowkeyVaultImageName())
            .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME))
            .vaultNames(Collections.singleton(VAULT_NAME))
            .vaultAliases(Map.of(VAULT_NAME + ".localhost", Set.of(ALIAS))).build()
            .withImagePullPolicy(PullPolicy.defaultPolicy());

    @Test
    void testContainerShouldStartUpWhenCalledWithValidNames() {
        //given + when test container is created

        //then
        final String endpoint = underTest.getVaultBaseUrl(VAULT_NAME);
        final AuthorityOverrideFunction authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getVaultAuthority(VAULT_NAME),
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final ApacheHttpClient httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithValidNamesUsingAlias() {
        //given + when test container is created

        //then
        final String endpoint = "https://" + ALIAS;
        final AuthorityOverrideFunction authorityOverrideFunction = new AuthorityOverrideFunction(
                ALIAS,
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final ApacheHttpClient httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }
}
