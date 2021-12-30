package com.github.nagyesta.lowkeyvault.testcontainers;

import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

@Testcontainers
class LowkeyVaultContainerJupiterTest extends AbstractLowkeyVaultContainerTest {

    private static final String VAULT_NAME = "jupiter";

    @Container
    private final LowkeyVaultContainer underTest = new LowkeyVaultContainer(DockerImageName
            .parse(getCurrentLowkeyVaultImageName())
            .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME), Collections.singleton(VAULT_NAME))
            .withImagePullPolicy(PullPolicy.defaultPolicy());

    @Test
    void testContainerShouldStartUpWhenCalledWithValidNames() {
        //given + when test container is created

        //then
        final String endpoint = underTest.getVaultBaseUrl(VAULT_NAME);
        final AuthorityOverrideFunction authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getVaultAuthority(VAULT_NAME),
                underTest.getEndpointAuthority());
        verifyConnectionIsWorking(endpoint, new ApacheHttpClient(authorityOverrideFunction), underTest.getCredentials());
    }
}
