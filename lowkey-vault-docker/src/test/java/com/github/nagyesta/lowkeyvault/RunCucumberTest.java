package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.context.TestContextConfig;
import io.cucumber.spring.CucumberContextConfiguration;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.springframework.context.annotation.Import;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@CucumberOptions(glue = "com.github.nagyesta.lowkeyvault",
        plugin = {"com.github.nagyesta.abortmission.booster.cucumber.AbortMissionPlugin"
                , "html:build/reports/cucumber/cucumber-report.html"})
@CucumberContextConfiguration
@Import(value = TestContextConfig.class)
@Test
public class RunCucumberTest extends AbstractTestNGCucumberTests {

    @DataProvider(parallel = true)
    @Override
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
