package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.abortmission.core.AbortMissionCommandOps;
import com.github.nagyesta.abortmission.core.healthcheck.MissionHealthCheckEvaluator;
import com.github.nagyesta.abortmission.core.matcher.MissionHealthCheckMatcher;
import com.github.nagyesta.abortmission.core.outline.MissionOutline;

import java.util.Map;
import java.util.function.Consumer;

import static com.github.nagyesta.abortmission.core.MissionControl.*;

public class MissionOutlineDefinition extends MissionOutline {

    @Override
    protected Map<String, Consumer<AbortMissionCommandOps>> defineOutline() {
        return Map.of(SHARED_CONTEXT, ops -> {
            final MissionHealthCheckMatcher integrationTestMatcher = matcher().classNamePattern(".*IntegrationTest").build();
            final MissionHealthCheckEvaluator integrationTests = percentageBasedEvaluator(integrationTestMatcher)
                    .overrideKeyword("IT")
                    .build();
            final MissionHealthCheckMatcher notIntegrationTest = matcher().not(integrationTestMatcher).build();
            final MissionHealthCheckMatcher test = matcher().classNamePattern(".*Test").build();
            final MissionHealthCheckEvaluator unitTests = reportOnlyEvaluator(matcher().and(notIntegrationTest).andAtLast(test).build())
                    .overrideKeyword("UT")
                    .build();
            final MissionHealthCheckEvaluator reportOnly = reportOnlyEvaluator(matcher().anyClass().build())
                    .overrideKeyword("all")
                    .build();
            ops.registerHealthCheck(integrationTests);
            ops.registerHealthCheck(unitTests);
            ops.registerHealthCheck(reportOnly);
        });
    }
}
