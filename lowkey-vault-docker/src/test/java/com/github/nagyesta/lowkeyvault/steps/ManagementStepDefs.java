package com.github.nagyesta.lowkeyvault.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.nagyesta.lowkeyvault.context.ManagementTestContext;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import com.github.nagyesta.lowkeyvault.http.AuthorityOverrideFunction;
import com.github.nagyesta.lowkeyvault.http.management.RecoveryLevel;
import com.github.nagyesta.lowkeyvault.http.management.VaultModel;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.apache.http.HttpException;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.CONTAINER_AUTHORITY;

public class ManagementStepDefs extends CommonAssertions {

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private ManagementTestContext context;

    @Given("a vault is created with name {name}")
    public void aVaultIsCreatedWithName(final String vaultName) throws HttpException, JsonProcessingException {
        final String vaultAuthority = vaultName + ".localhost:8443";
        final String vaultUrl = "https://" + vaultAuthority;
        final AuthorityOverrideFunction overrideFunction = new AuthorityOverrideFunction(vaultAuthority, CONTAINER_AUTHORITY);
        context.setProvider(new ApacheHttpClientProvider(vaultUrl, overrideFunction));
        context.getClient().createVault(URI.create(vaultUrl), RecoveryLevel.RECOVERABLE, 90);
    }

    @And("vault list contains {name} with deletedOn as null")
    public void vaultListContainsVaultNameWithDeletedOnAsNull(final String vaultName)
            throws HttpException, JsonProcessingException {
        final List<VaultModel> models = context.getClient().listVaults();
        final Optional<VaultModel> found = models.stream()
                .filter(v -> v.getBaseUri().getHost().startsWith(vaultName))
                .findAny();
        assertTrue(found.isPresent());
        //noinspection OptionalGetWithoutIsPresent
        final VaultModel vaultModel = found.get();
        assertNull(vaultModel.getDeletedOn());
        assertTrue(vaultModel.getCreatedOn().isBefore(OffsetDateTime.now()));
    }

    @And("deleted vault list does not contain {name}")
    public void deletedVaultListDoesNotContainVaultName(final String vaultName)
            throws HttpException, JsonProcessingException {
        final List<VaultModel> models = context.getClient().listDeletedVaults();
        final boolean missing = models.stream()
                .noneMatch(v -> v.getBaseUri().getHost().startsWith(vaultName));
        assertTrue(missing);
    }

    @When("the vault named {name} is deleted")
    public void theVaultNamedVaultNameIsDeleted(final String vaultName)
            throws HttpException, JsonProcessingException {
        final String vaultAuthority = vaultName + ".localhost:8443";
        final String vaultUrl = "https://" + vaultAuthority;
        final boolean deleted = context.getClient().delete(URI.create(vaultUrl));
        assertTrue(deleted);
    }

    @And("deleted vault list contains {name} with deletedOn populated")
    public void deletedVaultListContainsVaultNameWithDeletedOnPopulated(final String vaultName)
            throws HttpException, JsonProcessingException {
        final List<VaultModel> models = context.getClient().listDeletedVaults();
        final Optional<VaultModel> found = models.stream()
                .filter(v -> v.getBaseUri().getHost().startsWith(vaultName))
                .findAny();
        assertTrue(found.isPresent());
        //noinspection OptionalGetWithoutIsPresent
        final OffsetDateTime deletedOn = found.get().getDeletedOn();
        assertNotNull(deletedOn);
        assertTrue(deletedOn.isBefore(OffsetDateTime.now()));
    }

    @And("vault list does not contain {name}")
    public void vaultListDoesNotContainVaultName(final String vaultName)
            throws HttpException, JsonProcessingException {
        final List<VaultModel> models = context.getClient().listVaults();
        final boolean missing = models.stream()
                .noneMatch(v -> v.getBaseUri().getHost().startsWith(vaultName));
        assertTrue(missing);
    }

    @And("the vault named {name} is recovered")
    public void theVaultNamedVaultNameIsRecovered(final String vaultName)
            throws HttpException, JsonProcessingException {
        final String vaultAuthority = vaultName + ".localhost:8443";
        final String vaultUrl = "https://" + vaultAuthority;
        final VaultModel model = context.getClient().recover(URI.create(vaultUrl));
        assertNotNull(model);
    }
}
