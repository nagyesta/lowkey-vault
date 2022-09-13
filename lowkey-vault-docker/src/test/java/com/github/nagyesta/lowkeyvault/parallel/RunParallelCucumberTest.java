package com.github.nagyesta.lowkeyvault.parallel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.springframework.context.annotation.Import;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.DEFAULT_CONTAINER_URL;

@CucumberOptions(
        glue = {"com.github.nagyesta.lowkeyvault.parallel",
                "com.github.nagyesta.lowkeyvault.hook",
                "com.github.nagyesta.lowkeyvault.steps",
                "com.github.nagyesta.lowkeyvault.context"},
        features = {"classpath:/com/github/nagyesta/lowkeyvault/keys",
                "classpath:/com/github/nagyesta/lowkeyvault/secrets",
                "classpath:/com/github/nagyesta/lowkeyvault/certificates"},
        plugin = {"com.github.nagyesta.abortmission.booster.cucumber.AbortMissionPlugin",
                "html:build/reports/cucumber/cucumber-parallel-report.html"})
@CucumberContextConfiguration
@Import(value = TestContextConfig.class)
@Test(groups = "parallel")
public class RunParallelCucumberTest extends AbstractTestNGCucumberTests {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeSuite
    public void beforeSuit() throws InterruptedException {
        new ApacheHttpClientProvider(DEFAULT_CONTAINER_URL)
                .getLowkeyVaultManagementClient(OBJECT_MAPPER)
                .verifyConnectivity(30, 200,
                        () -> new AssertionError("Lowkey-Vault instance is not running."));
    }

    @DataProvider(parallel = true)
    @Override
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
