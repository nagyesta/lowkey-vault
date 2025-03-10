package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.core.credential.TokenCredential;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.lowkeyVault;

@SuppressWarnings("resource")
class LowkeyVaultContainerVanillaTest extends AbstractLowkeyVaultContainerTest {

    private static final String VAULT_NAME = "default";
    private static final int ALT_HOST_PORT = 18443;
    private static final int ALT_HOST_TOKEN_PORT = 18080;
    private static final int DEFAULT_PORT = 8443;
    private static final String LOCALHOST = "localhost";
    public static final String EXAMPLE_COM = "example.com";

    public static Stream<Arguments> invalidDataProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of((Collection<String>) null))
                .add(Arguments.of(Collections.singleton("")))
                .add(Arguments.of(Collections.singleton(" ")))
                .add(Arguments.of(Collections.singleton("- -")))
                .build();
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithValidNames() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName).vaultNames(Collections.singleton(VAULT_NAME)).build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var endpoint = underTest.getVaultBaseUrl(VAULT_NAME);
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getVaultAuthority(VAULT_NAME),
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithoutVaultNames() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName).build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var endpoint = underTest.getDefaultVaultBaseUrl();
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getDefaultVaultAuthority(),
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @SuppressWarnings("deprecation")
    @Test
    void testContainerShouldStartUpWhenCalledWithFixedHostPort() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName).hostPort(ALT_HOST_PORT).build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var endpoint = underTest.getDefaultVaultBaseUrl();
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getDefaultVaultAuthority(),
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithImportFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var importFile = new File(getClass().getResource("/full-import.json").getFile());
        final var underTest = lowkeyVault(imageName)
                .noAutoRegistration()
                .importFile(importFile)
                .debug()
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var endpoint = underTest.getDefaultVaultBaseUrl();
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getDefaultVaultAuthority(),
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithImportFileTemplate() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var importFile = new File(getClass().getResource("/full-import.json.hbs").getFile());
        final var underTest = lowkeyVault(imageName)
                .noAutoRegistration()
                .importFile(importFile)
                .logicalPort(DEFAULT_PORT)
                .logicalHost(LOCALHOST)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var endpoint = underTest.getDefaultVaultBaseUrl();
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                underTest.getDefaultVaultAuthority(),
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithImportFileTemplateAndCustomCertificate() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var importFile = new File(getClass().getResource("/full-import.json.hbs").getFile());
        //noinspection ConstantConditions
        final var certFile = new File(getClass().getResource("/cert.jks").getFile());
        final var underTest = lowkeyVault(imageName)
                .noAutoRegistration()
                .importFile(importFile)
                .logicalPort(DEFAULT_PORT)
                .logicalHost(EXAMPLE_COM)
                .customSslCertificate(certFile, "password", StoreType.JKS)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var authority = EXAMPLE_COM + ":" + underTest.getMappedPort(DEFAULT_PORT);
        final var endpoint = "https://" + authority;
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                authority,
                underTest.getEndpointAuthority());
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustAllStrategy(), new NoopHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithCustomCertificate() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var certFile = new File(getClass().getResource("/cert.jks").getFile());
        final var underTest = lowkeyVault(imageName)
                .additionalArgs(List.of("--logging.level.root=INFO"))
                .vaultAliases(Map.of(LOCALHOST, Set.of(EXAMPLE_COM)))
                .customSslCertificate(certFile, "password", StoreType.JKS)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var authority = EXAMPLE_COM;
        final var endpoint = "https://" + authority;
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                authority,
                underTest.getEndpointAuthority().replace(LOCALHOST, "127.0.0.1"));
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @ParameterizedTest
    @MethodSource("invalidDataProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithInvalidVaultNames(final Set<String> input) {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).vaultNames(input).build());

        //then + exceptions
    }

    @SuppressWarnings("deprecation")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidHostPortNumber() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).hostPort(0));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidLogicalPortNumber() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).logicalPort(0));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidLogicalHost() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).logicalHost(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidImportFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).importFile(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidCertFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lowkeyVault(imageName).customSslCertificate(null, null, null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidAliases() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lowkeyVault(imageName).vaultAliases(Map.of(LOCALHOST, Set.of("not valid"))));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidHost() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> lowkeyVault(imageName).vaultAliases(Map.of("not valid", Set.of(LOCALHOST))));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullAliases() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).vaultAliases(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullAdditionalArguments() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).additionalArgs(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidImageName() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullExternalConfigurationFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).externalConfigFile(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithAnExternalConfigurationFileThatIsNotAPropertiesFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        final var certFile = new File(Objects.requireNonNull(getClass().getResource("/cert.jks")).getFile());

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).externalConfigFile(certFile));

        //then + exceptions
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithExternalConfiguration() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var configFile = new File(getClass().getResource("/config.properties").getFile());
        final var underTest = lowkeyVault(imageName)
                .vaultAliases(Map.of(LOCALHOST, Set.of(EXAMPLE_COM)))
                .externalConfigFile(configFile)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var authority = EXAMPLE_COM;
        final var endpoint = "https://" + authority;
        final var authorityOverrideFunction = new AuthorityOverrideFunction(
                authority,
                underTest.getEndpointAuthority().replace(LOCALHOST, "127.0.0.1"));
        final TokenCredential credentials = new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword());
        final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyConnectionIsWorking(endpoint, httpClient, credentials);
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidHostTokenPortNumber() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).hostTokenPort(0));

        //then + exceptions
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithValidTokenPortConfiguration() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
       final var underTest = lowkeyVault(imageName)
                .vaultAliases(Map.of(LOCALHOST, Set.of(EXAMPLE_COM)))
                .hostTokenPort(ALT_HOST_TOKEN_PORT)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        underTest.start();

        //then
        final var endpoint = underTest.getTokenEndpointUrl();
        final var httpClient = new ApacheHttpClient(Function.identity(),
                new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
        verifyTokenEndpointIsWorking(endpoint, httpClient);
    }

}
