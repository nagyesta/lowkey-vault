package com.github.nagyesta.lowkeyvault.testcontainers;

import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

class LowkeyVaultContainerVanillaTest extends AbstractLowkeyVaultContainerTest {

    private static final String VAULT_NAME = "default";

    public static Stream<Arguments> invalidDataProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of((Collection<String>) null))
                .add(Arguments.of(Collections.singleton(null)))
                .add(Arguments.of(Collections.singleton("")))
                .add(Arguments.of(Collections.singleton(" ")))
                .add(Arguments.of(Collections.singleton("- -")))
                .build();
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithValidNames() {
        //given
        final LowkeyVaultContainer underTest = new LowkeyVaultContainer(DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME), Collections.singleton(VAULT_NAME))
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final String endpoint = underTest.getVaultBaseUrl(VAULT_NAME);
        final AuthorityOverrideFunction authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getVaultAuthority(VAULT_NAME),
                underTest.getEndpointAuthority());
        verifyConnectionIsWorking(endpoint, new ApacheHttpClient(authorityOverrideFunction), underTest.getCredentials());
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithoutVaultNames() {
        //given
        final LowkeyVaultContainer underTest = new LowkeyVaultContainer(DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME))
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final String endpoint = underTest.getDefaultVaultBaseUrl();
        final AuthorityOverrideFunction authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getDefaultVaultAuthority(),
                underTest.getEndpointAuthority());
        verifyConnectionIsWorking(endpoint, new ApacheHttpClient(authorityOverrideFunction), underTest.getCredentials());
    }

    @ParameterizedTest
    @MethodSource("invalidDataProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithInvalidVaultNames(final Set<String> input) {
        //given
        final DockerImageName dockerImageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new LowkeyVaultContainer(dockerImageName, input));

        //then + exceptions
    }

}
