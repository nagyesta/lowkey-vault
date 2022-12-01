package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.abortmission.core.AbortMissionCommandOps;
import com.github.nagyesta.abortmission.core.healthcheck.MissionHealthCheckEvaluator;
import com.github.nagyesta.abortmission.core.matcher.MissionHealthCheckMatcher;
import com.github.nagyesta.abortmission.core.outline.MissionOutline;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.nagyesta.abortmission.core.MissionControl.*;

public class MissionOutlineDefinition extends MissionOutline {

    private static Optional<Set<String>> annotationExtractor(final Object o) {
        if (!(o instanceof Class<?>)) {
            return Optional.empty();
        }
        final Class<?> testClass = (Class<?>) o;
        return Optional.of(Arrays.stream(testClass.getAnnotations())
                .map(annotation -> annotation.annotationType().getSimpleName()).collect(Collectors.toSet()));
    }

    @SuppressWarnings("checkstyle:MagicNumber")
    @Override
    protected Map<String, Consumer<AbortMissionCommandOps>> defineOutline() {
        return Map.of(SHARED_CONTEXT, ops -> {
            final MissionHealthCheckMatcher integrationTestMatcher = matcher().dependencyWith("SpringBootTest")
                    .extractor(MissionOutlineDefinition::annotationExtractor).build();
            final MissionHealthCheckEvaluator integrationTests = percentageBasedEvaluator(integrationTestMatcher)
                    .overrideKeyword("IT")
                    .burnInTestCount(1)
                    .abortThreshold(99)
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
