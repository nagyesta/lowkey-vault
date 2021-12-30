package com.github.nagyesta.lowkeyvault;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import com.github.nagyesta.lowkeyvault.http.ApacheHttpClientProvider;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Import;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Objects;

import static com.github.nagyesta.lowkeyvault.context.TestContextConfig.DEFAULT_CONTAINER_URL;

@CucumberOptions(glue = "com.github.nagyesta.lowkeyvault",
        plugin = {"com.github.nagyesta.abortmission.booster.cucumber.AbortMissionPlugin"
                , "html:build/reports/cucumber/cucumber-report.html"})
@CucumberContextConfiguration
@Import(value = TestContextConfig.class)
@Test
public class RunCucumberTest extends AbstractTestNGCucumberTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunCucumberTest.class);

    @BeforeSuite
    public void beforeSuit() {
        final HttpRequest request = new HttpRequest(HttpMethod.GET, DEFAULT_CONTAINER_URL + "/ping");
        final HttpClient client = new ApacheHttpClientProvider(DEFAULT_CONTAINER_URL).createInstance();
        for (int i = 0; i < 30; i++) {
            try {
                Thread.sleep(200);
                final HttpResponse response = client.send(request).block();
                final int statusCode = Objects.requireNonNull(response).getStatusCode();
                if (statusCode == HttpStatus.SC_OK) {
                    return;
                }
            } catch (final Exception e) {
                LOGGER.info("Container not available yet: {}", e.getMessage());
            }
        }
        Assert.fail("Lowkey-Vault instance is not running.");
    }

    @DataProvider(parallel = true)
    @Override
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
