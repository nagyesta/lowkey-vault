package com.github.nagyesta.lowkeyvault.controller.common;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.StandardServletEnvironment;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
class ControllerRequestMappingTest {

    @Test
    void testControllerEndpointShouldHaveBothMissingAndPresentTrailingSlashWhenAnnotatedWithGetMapping() {
        //given
        final Map<Class<?>, Map<Method, List<String>>> results = new HashMap<>();

        //when
        streamAllControllerClasses()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(GetMapping.class))
                .forEach(method -> {
                    final String[] pathMappings = method.getDeclaredAnnotation(GetMapping.class).value();
                    results.computeIfAbsent(method.getDeclaringClass(), k -> new HashMap<>()).put(method, Arrays.asList(pathMappings));
                });

        //then
        assertEveryMappingHasBothVersions(results);
    }

    @Test
    void testControllerEndpointShouldHaveBothMissingAndPresentTrailingSlashWhenAnnotatedWithPostMapping() {
        //given
        final Map<Class<?>, Map<Method, List<String>>> results = new HashMap<>();

        //when
        streamAllControllerClasses()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(PostMapping.class))
                .forEach(method -> {
                    final String[] pathMappings = method.getDeclaredAnnotation(PostMapping.class).value();
                    results.computeIfAbsent(method.getDeclaringClass(), k -> new HashMap<>()).put(method, Arrays.asList(pathMappings));
                });

        //then
        assertEveryMappingHasBothVersions(results);
    }

    @Test
    void testControllerEndpointShouldHaveBothMissingAndPresentTrailingSlashWhenAnnotatedWithPutMapping() {
        //given
        final Map<Class<?>, Map<Method, List<String>>> results = new HashMap<>();

        //when
        streamAllControllerClasses()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(PutMapping.class))
                .forEach(method -> {
                    final String[] pathMappings = method.getDeclaredAnnotation(PutMapping.class).value();
                    results.computeIfAbsent(method.getDeclaringClass(), k -> new HashMap<>()).put(method, Arrays.asList(pathMappings));
                });

        //then
        assertEveryMappingHasBothVersions(results);
    }

    @Test
    void testControllerEndpointShouldHaveBothMissingAndPresentTrailingSlashWhenAnnotatedWithPatchMapping() {
        //given
        final Map<Class<?>, Map<Method, List<String>>> results = new HashMap<>();

        //when
        streamAllControllerClasses()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(PatchMapping.class))
                .forEach(method -> {
                    final String[] pathMappings = method.getDeclaredAnnotation(PatchMapping.class).value();
                    results.computeIfAbsent(method.getDeclaringClass(), k -> new HashMap<>()).put(method, Arrays.asList(pathMappings));
                });

        //then
        assertEveryMappingHasBothVersions(results);
    }

    @Test
    void testControllerEndpointShouldHaveBothMissingAndPresentTrailingSlashWhenAnnotatedWithDeleteMapping() {
        //given
        final Map<Class<?>, Map<Method, List<String>>> results = new HashMap<>();

        //when
        streamAllControllerClasses()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(DeleteMapping.class))
                .forEach(method -> {
                    final String[] pathMappings = method.getDeclaredAnnotation(DeleteMapping.class).value();
                    results.computeIfAbsent(method.getDeclaringClass(), k -> new HashMap<>()).put(method, Arrays.asList(pathMappings));
                });

        //then
        assertEveryMappingHasBothVersions(results);
    }

    @Test
    void testControllerEndpointShouldHaveBothMissingAndPresentTrailingSlashWhenAnnotatedWithRequestMapping() {
        //given
        final Map<Class<?>, Map<Method, List<String>>> results = new HashMap<>();

        //when
        streamAllControllerClasses()
                .map(Class::getDeclaredMethods)
                .flatMap(Arrays::stream)
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(method -> {
                    final String[] pathMappings = method.getDeclaredAnnotation(RequestMapping.class).value();
                    results.computeIfAbsent(method.getDeclaringClass(), k -> new HashMap<>()).put(method, Arrays.asList(pathMappings));
                });

        //then
        assertEveryMappingHasBothVersions(results);
    }

    private static void assertEveryMappingHasBothVersions(final Map<Class<?>, Map<Method, List<String>>> results) {
        results.forEach((clazz, methodMap) -> {
            methodMap.forEach((method, pathMappings) -> {
                final String className = clazz.getName();
                final String methodName = method.getName();
                Assertions.assertFalse(pathMappings.isEmpty(),
                        "Method " + methodName + " should have the default path mappings in " + className + ".\n"
                                + "expected to have both: {\"\", \"/\"}");
                pathMappings.stream()
                        .filter(path -> !path.endsWith("/"))
                        .forEach(path -> {
                            Assertions.assertTrue(pathMappings.contains(path + "/"),
                                    "Method " + methodName + " should have a pair with trailing slash in " + className + ".\n"
                                            + "expected to have both: {\"" + path + "\", \"" + path + "/\"}\n"
                                            + "in: " + pathMappings + "\n");
                        });
            });
        });
    }

    private Stream<Class<?>> streamAllControllerClasses() {
        final String packageName = "com.github.nagyesta.lowkeyvault.controller";
        final ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(
                true, new StandardServletEnvironment());
        provider.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        return provider.findCandidateComponents(packageName).stream()
                .map(BeanDefinition::getBeanClassName)
                .map(className -> Assertions.assertDoesNotThrow(() -> Class.forName(className)));
    }
}
