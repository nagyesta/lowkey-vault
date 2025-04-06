package com.github.nagyesta.lowkeyvault.testcontainers;

import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
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
        verifyConnectionIsWorking(underTest, VAULT_NAME);
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithValidNamesUsingAlias() {
        //given + when test container is created

        //then
        final var endpoint = URI.create("https://" + ALIAS);
        verifyConnectionIsWorking(underTest, endpoint);
    }

    @Test
    void testContainerShouldProvideTokenEndpointWhenCalledWithValidParameters() {
        //given test container is created

        //when
        final var endpoint = underTest.getTokenEndpointUrl();

        //then
        final var httpClient = new ApacheHttpClient(uri -> uri,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyTokenEndpointIsWorking(endpoint, httpClient);
    }

    @Test
    void testContainerShouldProvideDefaultKeyStoreWhenRequested() throws Exception {
        //given test container is created

        //when
        final var password = underTest.getDefaultKeyStorePassword();
        final var keyStore = underTest.getDefaultKeyStore();

        //then
        Assertions.assertNotNull(keyStore);
        Assertions.assertNotNull(password);
        Assertions.assertTrue(keyStore.containsAlias(ALIAS), "Key store does not contain lowkey-vault.local");
        Assertions.assertNotNull(keyStore.getKey(ALIAS, password.toCharArray()), "Could not retrieve key from key store");
        Assertions.assertNotNull(keyStore.getCertificate(ALIAS));
    }
}
