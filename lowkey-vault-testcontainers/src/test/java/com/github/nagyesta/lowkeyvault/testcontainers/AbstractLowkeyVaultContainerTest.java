package com.github.nagyesta.lowkeyvault.testcontainers;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Context;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.certificates.models.CertificateContentType;
import com.azure.security.keyvault.certificates.models.CertificateKeyCurveName;
import com.azure.security.keyvault.certificates.models.CertificateKeyType;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.models.CreateOctKeyOptions;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import org.junit.jupiter.api.Assertions;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.fail;

public class AbstractLowkeyVaultContainerTest {

    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final int SUCCESS = 200;
    private static final int DEFAULT_HTTPS_PORT = 8443;

    protected String getCurrentLowkeyVaultImageName() {
        return "lowkey-vault:" + System.getProperty("imageVersion");
    }

    protected void verifyConnectionIsWorking(
            final LowkeyVaultContainer lowkeyVaultContainer) {
        final var clientFactory = lowkeyVaultContainer.getClientFactory();
        verifyConnectivity(clientFactory);
        verifyConnectionIsWorking(
                clientFactory.getSecretClientBuilderForDefaultVault(),
                clientFactory.getKeyClientBuilderForDefaultVault(),
                clientFactory.getCryptoClientBuilderForDefaultVault(),
                clientFactory.getCertificateClientBuilderForDefaultVault());
    }

    protected void verifyConnectionIsWorking(
            final LowkeyVaultContainer lowkeyVaultContainer,
            final String vaultName) {
        final var clientFactory = lowkeyVaultContainer.getClientFactory();
        verifyConnectivity(clientFactory);
        verifyConnectionIsWorking(
                clientFactory.getSecretClientBuilderFor(vaultName),
                clientFactory.getKeyClientBuilderFor(vaultName),
                clientFactory.getCryptoClientBuilderFor(vaultName),
                clientFactory.getCertificateClientBuilderFor(vaultName));
    }

    protected void verifyConnectionIsWorking(
            final LowkeyVaultContainer lowkeyVaultContainer,
            final URI vaultUri) {
        Assertions.assertNotEquals(DEFAULT_HTTPS_PORT, vaultUri.getPort(), "Vault URL should use random port instead of '8443'");
        final var clientFactory = lowkeyVaultContainer.getClientFactory();
        verifyConnectivity(clientFactory);
        verifyConnectionIsWorking(
                clientFactory.getSecretClientBuilderFor(vaultUri),
                clientFactory.getKeyClientBuilderFor(vaultUri),
                clientFactory.getCryptoClientBuilderFor(vaultUri),
                clientFactory.getCertificateClientBuilderFor(vaultUri));
    }


    protected void verifyConnectionIsWorking(
            final LowkeyVaultContainer lowkeyVaultContainer,
            final ApacheHttpClient httpClient,
            final URI vaultUri) {
        final var clientFactory = lowkeyVaultContainer.getClientFactory();
        verifyConnectionIsWorking(
                clientFactory.getSecretClientBuilderFor(vaultUri).httpClient(httpClient),
                clientFactory.getKeyClientBuilderFor(vaultUri).httpClient(httpClient),
                clientFactory.getCryptoClientBuilderFor(vaultUri).httpClient(httpClient),
                clientFactory.getCertificateClientBuilderFor(vaultUri).httpClient(httpClient));
    }

    private void verifyConnectionIsWorking(
            final SecretClientBuilder secretClientBuilder,
            final KeyClientBuilder keyClientBuilder,
            final CryptographyClientBuilder cryptographyClientBuilder,
            final CertificateClientBuilder certificateClientBuilder) {
        verifySecretCanBeSet(secretClientBuilder);
        verifyCertificateCanBeCreated(certificateClientBuilder);
        final var key = verifyKeyCanBeCreated(keyClientBuilder);
        verifyKeyCanBeUsedForCryptography(cryptographyClientBuilder, key);
    }

    private void verifyConnectivity(final LowkeyVaultClientFactory clientFactory) {
        try {
            clientFactory.getLowkeyVaultManagementClient().verifyConnectivity(1, 1, IllegalStateException::new);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            fail(e.getMessage());
        }
    }

    private static void verifyKeyCanBeUsedForCryptography(
            final CryptographyClientBuilder cryptographyClientBuilder,
            final KeyVaultKey key) {
        final var cryptographyClient = cryptographyClientBuilder
                .keyIdentifier(key.getId())
                .buildClient();
        final var testMessage = "test";
        final var encryptParameters = EncryptParameters.createA128CbcParameters(
                testMessage.getBytes(StandardCharsets.UTF_8), "iv-parameter-val".getBytes(StandardCharsets.UTF_8));
        final var encrypted = cryptographyClient
                .encrypt(encryptParameters, Context.NONE);
        final var decryptParameters = DecryptParameters
                .createA128CbcParameters(encrypted.getCipherText(), encrypted.getIv());
        final var decrypted = cryptographyClient
                .decrypt(decryptParameters, Context.NONE);

        Assertions.assertEquals(testMessage, new String(decrypted.getPlainText(), StandardCharsets.UTF_8));
    }

    private static KeyVaultKey verifyKeyCanBeCreated(
            final KeyClientBuilder keyClientBuilder) {
        final var keyOptions = new CreateOctKeyOptions(NAME)
                .setHardwareProtected(true)
                .setKeyOperations(KeyOperation.ENCRYPT, KeyOperation.WRAP_KEY, KeyOperation.DECRYPT, KeyOperation.UNWRAP_KEY);
        final var key = keyClientBuilder.buildClient().createOctKey(keyOptions);
        Assertions.assertNotNull(key);
        Assertions.assertNotNull(key.getId());
        return key;
    }

    private static void verifySecretCanBeSet(
            final SecretClientBuilder secretClientBuilder) {
        final var secret = secretClientBuilder.buildClient().setSecret(NAME, VALUE);
        Assertions.assertNotNull(secret);
        Assertions.assertNotNull(secret.getId());
    }

    private static void verifyCertificateCanBeCreated(
            final CertificateClientBuilder certificateClientBuilder) {
        final var subject = "CN=example.com";
        final var policy = new CertificatePolicy("Self", subject)
                .setKeyType(CertificateKeyType.EC)
                .setKeyCurveName(CertificateKeyCurveName.P_256)
                .setContentType(CertificateContentType.PKCS12);
        final var cert = certificateClientBuilder.buildClient()
                .beginCreateCertificate("cert-example-com", policy).waitForCompletion().getValue();
        Assertions.assertNotNull(cert);
        Assertions.assertNotNull(cert.getId());
    }

    protected void verifyTokenEndpointIsWorking(final String endpointUrl, final HttpClient httpClient) {
        httpClient.send(new HttpRequest(HttpMethod.GET, endpointUrl + "?resource=https://localhost"))
                .doOnError(Assertions::fail)
                .doOnSuccess(response -> Assertions.assertEquals(SUCCESS, response.getStatusCode()))
                .single()
                .block();
    }
}
