package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.abortmission.booster.jupiter.extractor.TagDependencyNameExtractor;
import com.github.nagyesta.abortmission.core.AbortMissionCommandOps;
import com.github.nagyesta.abortmission.core.healthcheck.MissionHealthCheckEvaluator;
import com.github.nagyesta.abortmission.core.matcher.MissionHealthCheckMatcher;
import com.github.nagyesta.abortmission.core.outline.MissionOutline;

import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.nagyesta.abortmission.core.MissionControl.matcher;
import static com.github.nagyesta.abortmission.core.MissionControl.percentageBasedEvaluator;

public class MissionOutlineDefinition extends MissionOutline {

    private static final int ABORT_THRESHOLD = 70;
    private static final int TOTAL_BURN_IN_TEST_COUNT = 5;
    private static final int BURN_IN_TEST_COUNT = 3;
    private static final int TAG_BURN_IN_TEST_COUNT = 1;

    @Override
    protected Map<String, Consumer<AbortMissionCommandOps>> defineOutline() {
        return Map.of(SHARED_CONTEXT, ops -> {
            final MissionHealthCheckEvaluator classPercentage = percentageBasedEvaluator(matcher().anyClass().build())
                    .abortThreshold(ABORT_THRESHOLD)
                    .burnInTestCount(TOTAL_BURN_IN_TEST_COUNT)
                    .build();
            ops.registerHealthCheck(classPercentage);
            final TagDependencyNameExtractor extractor = new TagDependencyNameExtractor();
            final MissionHealthCheckMatcher createMatcher = matcher().dependencyWith("create").extractor(extractor).build();
            final MissionHealthCheckEvaluator createTagPercentage = percentageBasedEvaluator(createMatcher)
                    .abortThreshold(ABORT_THRESHOLD)
                    .burnInTestCount(BURN_IN_TEST_COUNT)
                    .build();
            ops.registerHealthCheck(createTagPercentage);
            Stream.of("rsa", "ec", "oct").forEach(tag -> {
                final MissionHealthCheckMatcher matcher = matcher().dependencyWith(tag).extractor(extractor).build();
                final MissionHealthCheckEvaluator tagPercentage = percentageBasedEvaluator(matcher)
                        .abortThreshold(ABORT_THRESHOLD)
                        .burnInTestCount(TAG_BURN_IN_TEST_COUNT)
                        .build();
                ops.registerHealthCheck(tagPercentage);
            });
        });
    }
}
