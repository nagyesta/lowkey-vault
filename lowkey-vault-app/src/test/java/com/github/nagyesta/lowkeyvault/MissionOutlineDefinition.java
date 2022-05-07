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
                    .build();
            final MissionHealthCheckEvaluator reportOnly = reportOnlyEvaluator(matcher().anyClass().build())
                    .build();
            ops.registerHealthCheck(integrationTests);
            ops.registerHealthCheck(reportOnly);
        });
    }
}
