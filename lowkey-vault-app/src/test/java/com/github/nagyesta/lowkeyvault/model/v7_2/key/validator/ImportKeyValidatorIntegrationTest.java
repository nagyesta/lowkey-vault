package com.github.nagyesta.lowkeyvault.model.v7_2.key.validator;

import com.github.nagyesta.abortmission.booster.jupiter.annotation.LaunchAbortArmed;
import com.github.nagyesta.lowkeyvault.ResourceUtils;
import com.github.nagyesta.lowkeyvault.model.v7_2.key.request.ImportKeyRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@LaunchAbortArmed
@SpringBootTest
@TestPropertySource(locations = "classpath:test-application.properties")
class ImportKeyValidatorIntegrationTest {

    @Autowired
    private Validator validator;
    @Autowired
    private ObjectMapper objectMapper;

    public static Stream<Arguments> invalidProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("/key/import/aes-import-missing-kty.json", "keyType"))
                .add(Arguments.of("/key/import/aes-import-missing-k.json", "k"))
                .add(Arguments.of("/key/import/ec-import-missing-crv.json", "curveName"))
                .add(Arguments.of("/key/import/ec-import-missing-d.json", "d"))
                .add(Arguments.of("/key/import/ec-import-missing-x.json", "x"))
                .add(Arguments.of("/key/import/ec-import-missing-y.json", "y"))
                .add(Arguments.of("/key/import/rsa-import-missing-d.json", "d"))
                .add(Arguments.of("/key/import/rsa-import-missing-dp.json", "dp"))
                .add(Arguments.of("/key/import/rsa-import-missing-dq.json", "dq"))
                .add(Arguments.of("/key/import/rsa-import-missing-e.json", "e"))
                .add(Arguments.of("/key/import/rsa-import-missing-n.json", "n"))
                .add(Arguments.of("/key/import/rsa-import-missing-p.json", "p"))
                .add(Arguments.of("/key/import/rsa-import-missing-q.json", "q"))
                .add(Arguments.of("/key/import/rsa-import-missing-qi.json", "qi"))
                .build();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/key/import/ec-import-valid.json",
            "/key/import/aes-import-valid.json",
            "/key/import/rsa-import-valid.json"
    })
    void testValidateShouldUseRightGroupWhenCalledWithValidPayload(final String resource) {
        //given
        final var input = loadResourceAsObject(resource);

        //when
        final var violations = validator.validate(input);

        //then
        Assertions.assertNotNull(violations);
        Assertions.assertEquals(Collections.emptySet(), violations);
    }

    @ParameterizedTest
    @MethodSource("invalidProvider")
    void testValidateShouldUseRightGroupAndMarkInvalidPropertiesWhenCalledWithInvalidPayload(
            final String resource, final String property) {
        //given
        final var input = loadResourceAsObject(resource);

        //when
        final var violations = validator.validate(input);

        //then
        Assertions.assertNotNull(violations);
        Assertions.assertFalse(violations.isEmpty());
        final var properties = violations.stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Path::toString)
                .collect(Collectors.toSet());
        Assertions.assertEquals(Set.of("key", "key." + property), properties);
    }

    private ImportKeyRequest loadResourceAsObject(final String resource) {
        final var json = ResourceUtils.loadResourceAsString(resource);
        return objectMapper.readerFor(ImportKeyRequest.class).readValue(json);
    }
}
