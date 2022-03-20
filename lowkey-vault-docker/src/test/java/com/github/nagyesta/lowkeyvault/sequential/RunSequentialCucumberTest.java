package com.github.nagyesta.lowkeyvault.sequential;

import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.springframework.context.annotation.Import;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@CucumberOptions(
        glue = {"com.github.nagyesta.lowkeyvault.sequential",
                "com.github.nagyesta.lowkeyvault.hook",
                "com.github.nagyesta.lowkeyvault.steps",
                "com.github.nagyesta.lowkeyvault.context"},
        features = {"classpath:/com/github/nagyesta/lowkeyvault/management"},
        plugin = {"com.github.nagyesta.abortmission.booster.cucumber.AbortMissionPlugin",
                "html:build/reports/cucumber/cucumber-sequential-report.html"})
@CucumberContextConfiguration
@Import(value = TestContextConfig.class)
@Test(dependsOnGroups = "parallel")
public class RunSequentialCucumberTest extends AbstractTestNGCucumberTests {

    @DataProvider
    @Override
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
