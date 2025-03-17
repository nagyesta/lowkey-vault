package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer.DEFAULT_IMAGE_NAME;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

@Isolated
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
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName).vaultNames(Collections.singleton(VAULT_NAME)).build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            verifyConnectionIsWorking(underTest, VAULT_NAME);
        }
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithoutVaultNames() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName).build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            verifyConnectionIsWorking(underTest);
        }
    }

    @SuppressWarnings("deprecation")
    @Test
    void testContainerShouldStartUpWhenCalledWithFixedHostPort() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName).hostPort(ALT_HOST_PORT).build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            verifyConnectionIsWorking(underTest);
        }
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithImportFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var importFile = new File(getClass().getResource("/full-import.json").getFile());
        final var underTest = lowkeyVault(imageName)
                .noAutoRegistration()
                .importFile(importFile)
                .debug()
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            verifyConnectionIsWorking(underTest);
        }
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithImportFileTemplate() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
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
        try (underTest) {
            underTest.start();

            //then
            verifyConnectionIsWorking(underTest);
        }
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithImportFileTemplateAndCustomCertificate() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
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
        try (underTest) {
            underTest.start();

            //then
            final var authority = EXAMPLE_COM + ":" + underTest.getMappedPort(DEFAULT_PORT);
            final var endpoint = "https://" + authority;
            final var authorityOverrideFunction = new AuthorityOverrideFunction(
                    authority,
                    underTest.getEndpointAuthority());
            final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                    new TrustAllStrategy(), new NoopHostnameVerifier());
            verifyConnectionIsWorking(underTest, httpClient, URI.create(endpoint));
        }
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithCustomCertificate() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var certFile = new File(getClass().getResource("/cert.jks").getFile());
        final var underTest = lowkeyVault(imageName)
                .additionalArgs(List.of("--logging.level.root=INFO"))
                .vaultAliases(Map.of(LOCALHOST, Set.of(EXAMPLE_COM)))
                .customSslCertificate(certFile, "password", StoreType.JKS)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            final var endpoint = "https://" + EXAMPLE_COM;
            final var authorityOverrideFunction = new AuthorityOverrideFunction(
                    EXAMPLE_COM,
                    underTest.getEndpointAuthority().replace(LOCALHOST, "127.0.0.1"));
            final var httpClient = new ApacheHttpClient(authorityOverrideFunction,
                    new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
            verifyConnectionIsWorking(underTest, httpClient, URI.create(endpoint));
        }
    }

    @ParameterizedTest
    @MethodSource("invalidDataProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithInvalidVaultNames(final Set<String> input) {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

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
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).hostPort(0));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidLogicalPortNumber() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).logicalPort(0));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidLogicalHost() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).logicalHost(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidImportFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).importFile(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidCertFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

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
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

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
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

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
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).vaultAliases(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullAdditionalArguments() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).additionalArgs(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidImageName() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault((DockerImageName) null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullExternalConfigurationFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).externalConfigFile(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithAnExternalConfigurationFileThatIsNotAPropertiesFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
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
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var configFile = new File(getClass().getResource("/config.properties").getFile());
        final var underTest = lowkeyVault(imageName)
                .vaultAliases(Map.of(LOCALHOST, Set.of(EXAMPLE_COM)))
                .externalConfigFile(configFile)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            final var endpoint = URI.create("https://" + EXAMPLE_COM);
            verifyConnectionIsWorking(underTest, endpoint);
        }
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidHostTokenPortNumber() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).hostTokenPort(0));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullImage() {
        //given
        final String imageName = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName).hostTokenPort(0));

        //then + exceptions
    }

    @Test
    void testContainerShouldStartUpWhenCalledWithValidTokenPortConfiguration() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName)
                .vaultAliases(Map.of(LOCALHOST, Set.of(EXAMPLE_COM)))
                .hostTokenPort(ALT_HOST_TOKEN_PORT)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            final var endpoint = underTest.getTokenEndpointUrl();
            final var httpClient = new ApacheHttpClient(Function.identity(),
                    new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
            verifyTokenEndpointIsWorking(endpoint, httpClient);
        }
    }

    @Test
    void testContainerShouldStartWhenItDependsOnJdbcContainer() {
        //given
        final var mysql = new MySQLContainer<>("mysql:9.2.0");

        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName)
                .dependsOnContainer(mysql)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            underTest.getClientFactory()
                    .getLowkeyVaultManagementClient()
                    .verifyConnectivity(1, 1, IllegalStateException::new);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(e.getMessage());
        }
    }

    @Test
    void testContainerShouldSetSecretsAfterStartUpWhenItDependsOnJdbcContainer() {
        //given
        final var mysql = new MySQLContainer<>("mysql:9.2.0");

        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName)
                .dependsOnContainer(mysql, springJdbcSecretSupplier())
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            final var secretClient = underTest.getClientFactory()
                    .getSecretClientBuilderForDefaultVault()
                    .buildClient();
            final var actualUrl = secretClient.getSecret("spring-datasource-url").getValue();
            final var actualDriverClassName = secretClient.getSecret("spring-datasource-driver-class-name").getValue();
            final var actualUsername = secretClient.getSecret("spring-datasource-username").getValue();
            final var actualPassword = secretClient.getSecret("spring-datasource-password").getValue();
            assertEquals(mysql.getJdbcUrl(), actualUrl);
            assertEquals(mysql.getDriverClassName(), actualDriverClassName);
            assertEquals(mysql.getUsername(), actualUsername);
            assertEquals(mysql.getPassword(), actualPassword);
        }
    }

    @Test
    void testContainerShouldSetSecretsAfterStartUpWhenItDependsOnJdbcContainerAndTrustStoresAreMerged() {
        //given
        final var mysql = new MySQLContainer<>("mysql:9.2.0");

        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName)
                .mergeTrustStores()
                .dependsOnContainer(mysql, springJdbcSecretSupplier())
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            final var secretClient = underTest.getClientFactory()
                    .getSecretClientBuilderForDefaultVault()
                    .buildClient();
            final var actualUrl = secretClient.getSecret("spring-datasource-url").getValue();
            final var actualDriverClassName = secretClient.getSecret("spring-datasource-driver-class-name").getValue();
            final var actualUsername = secretClient.getSecret("spring-datasource-username").getValue();
            final var actualPassword = secretClient.getSecret("spring-datasource-password").getValue();
            assertEquals(mysql.getJdbcUrl(), actualUrl);
            assertEquals(mysql.getDriverClassName(), actualDriverClassName);
            assertEquals(mysql.getUsername(), actualUsername);
            assertEquals(mysql.getPassword(), actualPassword);
        }
    }

    @Test
    void testContainerShouldSetSystemPropertiesAndMergeTrustStoreAfterStartUpWhenRequested() {
        //given
        System.clearProperty(KeyStoreMerger.BACKUP_TRUST_STORE_LOCATION_PROPERTY);
        System.clearProperty(KeyStoreMerger.BACKUP_TRUST_STORE_TYPE_PROPERTY);
        System.clearProperty(KeyStoreMerger.BACKUP_TRUST_STORE_PASSWORD_PROPERTY);
        final var underTest = lowkeyVault(DEFAULT_IMAGE_NAME.withTag("2.13.0").toString())
                .setPropertiesAfterStartup(springCloudAzureKeyVaultPropertySupplier())
                .mergeTrustStores()
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        //when
        try (underTest) {
            underTest.start();

            //then
            final var actualUrl = System.getProperty("spring.cloud.azure.keyvault.secret.property-sources[0].endpoint");
            final var actualCRV = System.getProperty(
                    "spring.cloud.azure.keyvault.secret.property-sources[0].challenge-resource-verification-enabled");
            assertEquals(underTest.getDefaultVaultBaseUrl(), actualUrl);
            assertEquals("false", actualCRV);
            final var secretClient = new SecretClientBuilder()
                    .vaultUrl(actualUrl)
                    .disableChallengeResourceVerification()
                    .credential(new BasicAuthenticationCredential(underTest.getUsername(), underTest.getPassword()))
                    .buildClient();
            final var secret = secretClient.setSecret(VAULT_NAME, VAULT_NAME);
            assertNotNull(secret);
            assertNotNull(secret.getId());
        }
    }

}
