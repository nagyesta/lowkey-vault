package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.credential.BasicAuthenticationCredential;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.SecretProperties;
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
import org.testcontainers.images.PullPolicy;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainer.DEFAULT_IMAGE_NAME;
import static com.github.nagyesta.lowkeyvault.testcontainers.LowkeyVaultContainerBuilder.*;
import static org.junit.jupiter.api.Assertions.*;

@Isolated
class LowkeyVaultContainerVanillaTest extends AbstractLowkeyVaultContainerTest {

    private static final String VAULT_NAME = "default";
    private static final int ALT_HOST_TOKEN_PORT = 18080;
    private static final int DEFAULT_PORT = 8443;
    private static final String LOCALHOST = "localhost";
    private static final String EXAMPLE_COM = "example.com";
    private static final String MYSQL_IMAGE_NAME = "mysql:9.2.0";

    public static Stream<Arguments> invalidDataProvider() {
        return Stream.<Arguments>builder()
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
    void testContainerShouldLoadPreviouslyExportedStateWhenCalledWithEmptyImportFileActivatedPersistenceAndTheContainerIsRestarted()
            throws IOException {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        //noinspection ConstantConditions
        final var importFile = getTempPath().toFile();
        //noinspection ConstantConditions
        Files.copy(Path.of(getClass().getResource("/empty-import.json").getFile()), importFile.toPath());
        final var underTest = lowkeyVault(imageName)
                .noAutoRegistration()
                .persistent(importFile)
                .build()
                .withImagePullPolicy(PullPolicy.defaultPolicy());

        try (underTest) {
            underTest.start();
            verifyConnectionIsWorking(underTest);
            //modifications are saved
            underTest.stop();

            //when
            underTest.start();

            //then
            final var clientFactory = underTest.getClientFactory();
            verifyConnectivity(clientFactory);
            final var actualSecretNames = clientFactory.getSecretClientBuilderForDefaultVault()
                    .buildClient()
                    .listPropertiesOfSecrets()
                    .stream()
                    .map(SecretProperties::getName)
                    .toList();
            final var actualKeyNames = clientFactory.getKeyClientBuilderForDefaultVault()
                    .buildClient()
                    .listPropertiesOfKeys()
                    .stream()
                    .map(KeyProperties::getName)
                    .toList();
            final var actualCertificateNames = clientFactory.getCertificateClientBuilderForDefaultVault()
                    .buildClient()
                    .listPropertiesOfCertificates()
                    .stream()
                    .map(CertificateProperties::getName)
                    .toList();

            Assertions.assertIterableEquals(List.of(CERT_EXAMPLE_COM, NAME), actualSecretNames);
            Assertions.assertIterableEquals(List.of(CERT_EXAMPLE_COM, NAME), actualKeyNames);
            Assertions.assertIterableEquals(List.of(CERT_EXAMPLE_COM), actualCertificateNames);
        } finally {
            Files.deleteIfExists(importFile.toPath());
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

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNulVaultNames() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(NullPointerException.class, () -> builder.vaultNames(null));

        //then + exceptions
    }

    @ParameterizedTest
    @MethodSource("invalidDataProvider")
    void testConstructorShouldThrowExceptionWhenCalledWithInvalidVaultNames(final Set<String> input) {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName).vaultNames(input);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, builder::build);

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidLogicalPortNumber() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.logicalPort(0));

        //then + exceptions
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidLogicalHost() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(NullPointerException.class, () -> builder.logicalHost(null));

        //then + exceptions
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidImportFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(NullPointerException.class, () -> builder.importFile(null));

        //then + exceptions
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidCertFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(NullPointerException.class,
                () -> builder.customSslCertificate(null, null, null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidAliases() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);
        final var notValid = Map.of(LOCALHOST, Set.of("not valid"));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.vaultAliases(notValid));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithInvalidHost() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);
        final var notValid = Map.of("not valid", Set.of(LOCALHOST));

        //when
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> builder.vaultAliases(notValid));

        //then + exceptions
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullAliases() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(NullPointerException.class, () -> builder.vaultAliases(null));

        //then + exceptions
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullAdditionalArguments() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(NullPointerException.class, () -> builder.additionalArgs(null));

        //then + exceptions
    }

    @SuppressWarnings("DataFlowIssue")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullExternalConfigurationFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(NullPointerException.class, () -> builder.externalConfigFile(null));

        //then + exceptions
    }

    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithAnExternalConfigurationFileThatIsNotAPropertiesFile() {
        //given
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var certFile = new File(Objects.requireNonNull(getClass().getResource("/cert.jks")).getFile());
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.externalConfigFile(certFile));

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
        final var builder = lowkeyVault(imageName);

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.hostTokenPort(0));

        //then + exceptions
    }

    @SuppressWarnings("ConstantValue")
    @Test
    void testBuilderShouldThrowExceptionWhenCalledWithNullImage() {
        //given
        final String imageName = null;

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> lowkeyVault(imageName));

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
            final var httpClient = new ApacheHttpClient(uri -> uri,
                    new TrustSelfSignedStrategy(), new DefaultHostnameVerifier());
            verifyTokenEndpointIsWorking(endpoint, httpClient);
        }
    }

    @Test
    void testContainerShouldStartWhenItDependsOnJdbcContainer() {
        //given
        final var mysql = new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE_NAME));

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
        final var mysql = new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE_NAME));

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
        final var mysql = new MySQLContainer(DockerImageName.parse(MYSQL_IMAGE_NAME));

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
        final var imageName = DockerImageName
                .parse(getCurrentLowkeyVaultImageName())
                .asCompatibleSubstituteFor(DEFAULT_IMAGE_NAME);
        final var underTest = lowkeyVault(imageName)
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
            assertEquals(underTest.getEndpointBaseUrl(), actualUrl);
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
