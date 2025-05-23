package com.github.nagyesta.lowkeyvault.steps;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.github.nagyesta.lowkeyvault.context.ManagementTestContext;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClient;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import com.github.nagyesta.lowkeyvault.http.management.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.http.management.TimeShiftContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.CONTAINER_AUTHORITY;

public class ManagementStepDefs extends CommonAssertions {

    private final ManagementTestContext context;

    public ManagementStepDefs(final TestContextConfig config) {
        this.context = config.managementContext();
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @When("OpenAPI ui is available")
    public void openApiUiIsAvailable() throws MalformedURLException {
        final var swaggerUri = URI.create("https://" + CONTAINER_AUTHORITY+"/api/swagger-ui/index.html");
        final var client = new ApacheHttpClient(uri -> uri, new TrustSelfSignedStrategy(), new NoopHostnameVerifier());
        final var response = client.send(new HttpRequest(HttpMethod.GET, swaggerUri.toURL())).block();
        assertNotNull(response);
        assertEquals(200, Objects.requireNonNull(response).getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBodyAsString().block()).contains("<title>Swagger UI</title>"));
    }

    @Given("a vault is created with name {name}")
    public void aVaultIsCreatedWithName(final String vaultName) {
        final var vaultAuthority = vaultName + ".localhost:8443";
        final var vaultUrl = "https://" + vaultAuthority;
        final var overrideFunction = new AuthorityOverrideFunction(vaultAuthority, CONTAINER_AUTHORITY);
        context.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
        context.getClient().createVault(URI.create(vaultUrl), RecoveryLevel.RECOVERABLE_AND_PURGEABLE, 90);
    }

    @And("vault list contains {name} with deletedOn as null")
    public void vaultListContainsVaultNameWithDeletedOnAsNull(final String vaultName) {
        final var models = context.getClient().listVaults();
        final var found = models.stream()
                .filter(v -> v.getBaseUri().getHost().startsWith(vaultName))
                .findAny();
        assertTrue(found.isPresent());
        //noinspection OptionalGetWithoutIsPresent
        final var vaultModel = found.get();
        assertNull(vaultModel.getDeletedOn());
        assertTrue(vaultModel.getCreatedOn().isBefore(OffsetDateTime.now()));
    }

    @And("deleted vault list does not contain {name}")
    public void deletedVaultListDoesNotContainVaultName(final String vaultName) {
        final var models = context.getClient().listDeletedVaults();
        final var missing = models.stream()
                .noneMatch(v -> v.getBaseUri().getHost().startsWith(vaultName));
        assertTrue(missing);
    }

    @When("the vault named {name} is deleted")
    public void theVaultNamedVaultNameIsDeleted(final String vaultName) {
        final var vaultUrl = vaultNameToUrl(vaultName);
        final var deleted = context.getClient().delete(URI.create(vaultUrl));
        assertTrue(deleted);
    }

    @And("deleted vault list contains {name} with deletedOn populated")
    public void deletedVaultListContainsVaultNameWithDeletedOnPopulated(final String vaultName) {
        final var models = context.getClient().listDeletedVaults();
        final var found = models.stream()
                .filter(v -> v.getBaseUri().getHost().startsWith(vaultName))
                .findAny();
        assertTrue(found.isPresent());
        //noinspection OptionalGetWithoutIsPresent
        final var deletedOn = found.get().getDeletedOn();
        assertNotNull(deletedOn);
        assertTrue(deletedOn.isBefore(OffsetDateTime.now()));
    }

    @And("vault list does not contain {name}")
    public void vaultListDoesNotContainVaultName(final String vaultName) {
        final var models = context.getClient().listVaults();
        final var missing = models.stream()
                .noneMatch(v -> v.getBaseUri().getHost().startsWith(vaultName));
        assertTrue(missing);
    }

    @And("the vault named {name} is recovered")
    public void theVaultNamedVaultNameIsRecovered(final String vaultName) {
        final var vaultUrl = vaultNameToUrl(vaultName);
        final var model = context.getClient().recover(URI.create(vaultUrl));
        assertNotNull(model);
    }

    @And("the vault named {name} is purged")
    public void theVaultNamedVaultNameIsPurged(final String vaultName) {
        final var vaultUrl = vaultNameToUrl(vaultName);
        final var model = context.getClient().purge(URI.create(vaultUrl));
        assertTrue(model);
    }

    @When("the time of the vault named {name} is shifted by {int} days")
    public void theTimeOfTheVaultNamedVaultNameIsShiftedByDays(final String vaultName, final int timeShiftDays) {
        final var vaultUrl = vaultNameToUrl(vaultName);
        context.getClient().timeShift(TimeShiftContext.builder()
                .vaultBaseUri(URI.create(vaultUrl))
                .regenerateCertificates()
                .addDays(timeShiftDays)
                .build());
    }

    @When("the time of all vaults is shifted by {int} days")
    public void theTimeOfAllVaultsIsShiftedByDays(final int timeShiftDays) {
        context.getClient().timeShift(TimeShiftContext.builder()
                .addDays(timeShiftDays)
                .build());
    }

    private String vaultNameToUrl(final String vaultName) {
        final var vaultAuthority = vaultName + ".localhost:8443";
        return "https://" + vaultAuthority;
    }

    @And("vault list is saved as {name}")
    public void vaultListIsSavedAsName(final String name) {
        context.getVaultLists().put(name, context.getClient().listVaults());
    }

    @Then("the time stamps of {name} and {name} differ by {int} days")
    public void theTimeStampsOfOriginalAndUpdatedDifferByDays(final String original, final String updated, final int expectedDays) {
        final var originalList = context.getVaultLists().getOrDefault(original, Collections.emptyList());
        final var updatedList = context.getVaultLists().getOrDefault(updated, Collections.emptyList());
        final Map<URI, OffsetDateTime> createdMap = new HashMap<>();
        originalList.forEach(vaultModel -> createdMap.put(vaultModel.getBaseUri(), vaultModel.getCreatedOn().minusDays(expectedDays)));
        updatedList.forEach(vaultModel -> assertEquals(createdMap.get(vaultModel.getBaseUri()), vaultModel.getCreatedOn()));
    }
}
