package com.github.nagyesta.lowkeyvault.hook;

import com.github.nagyesta.abortmission.booster.cucumber.LaunchAbortHook;
import com.github.nagyesta.abortmission.booster.cucumber.matcher.TagDependencyNameExtractor;
import com.github.nagyesta.abortmission.core.AbortMissionCommandOps;
import com.github.nagyesta.abortmission.core.healthcheck.MissionHealthCheckEvaluator;
import com.github.nagyesta.abortmission.core.matcher.MissionHealthCheckMatcher;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.testng.SkipException;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.nagyesta.abortmission.core.MissionControl.*;
import static com.github.nagyesta.abortmission.core.outline.MissionOutline.SHARED_CONTEXT;

public class MissionOutlineDefinition extends LaunchAbortHook {

    private static final int ABORT_THRESHOLD = 70;
    private static final int TOTAL_BURN_IN_TEST_COUNT = 5;
    private static final int BURN_IN_TEST_COUNT = 1;

    @Override
    protected Map<String, Consumer<AbortMissionCommandOps>> defineOutline() {
        return Map.of(SHARED_CONTEXT, ops -> {
            final TagDependencyNameExtractor extractor = new TagDependencyNameExtractor();
            ops.registerHealthCheck(reportOnlyEvaluator(anyScenarioMatcher()).overrideKeyword("all").build());
            Stream.of("Key", "Secret", "Certificate").forEach(type -> {
                final MissionHealthCheckMatcher typeMatcher = matcher().dependencyWith(type).extractor(extractor).build();
                final MissionHealthCheckEvaluator featurePercentage = percentageBasedEvaluator(typeMatcher)
                        .abortThreshold(ABORT_THRESHOLD)
                        .burnInTestCount(TOTAL_BURN_IN_TEST_COUNT)
                        .overrideKeyword(type)
                        .build();
                ops.registerHealthCheck(featurePercentage);

                Stream.of("Create", "Get", "Delete", "List", "Update", "ListDeleted", "Recover", "Purge", "Backup", "Restore", "Alias")
                        .forEach(subtype -> {
                            final MissionHealthCheckMatcher subTypeMatcher = matcher().dependencyWith(type + subtype)
                                    .extractor(extractor).build();
                            final MissionHealthCheckEvaluator subFeaturePercentage = percentageBasedEvaluator(subTypeMatcher)
                                    .abortThreshold(ABORT_THRESHOLD)
                                    .burnInTestCount(BURN_IN_TEST_COUNT)
                                    .overrideKeyword(type + subtype)
                                    .build();
                            ops.registerHealthCheck(subFeaturePercentage);
                        });
            });

            Stream.of("CreateVault", "KeyRotate", "KeyImport", "KeyEncrypt", "KeySign", "RSA", "EC", "OCT").forEach(tag -> {
                final MissionHealthCheckMatcher matcher = matcher().dependencyWith(tag).extractor(extractor).build();
                final MissionHealthCheckEvaluator tagPercentage = percentageBasedEvaluator(matcher)
                        .abortThreshold(ABORT_THRESHOLD)
                        .burnInTestCount(BURN_IN_TEST_COUNT)
                        .overrideKeyword(tag)
                        .build();
                ops.registerHealthCheck(tagPercentage);
            });
        });
    }

    @Before
    @Override
    public void beforeScenario(final Scenario scenario) {
        doBeforeScenario(scenario);
    }

    @After
    @Override
    public void afterScenario(final Scenario scenario) {
        doAfterScenario(scenario);
    }

    @Override
    protected void doAbort() {
        throw new SkipException("Aborting as the launch failure threshold is reached.");
    }
}
