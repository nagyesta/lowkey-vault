package com.github.nagyesta.lowkeyvault.testcontainers;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import org.testcontainers.utility.DockerImageName;

import java.util.stream.Stream;

import static org.mockito.Mockito.*;

class LowkeyVaultContainerTest extends AbstractLowkeyVaultContainerTest {

    public static Stream<Arguments> imageRecommendationInputProvider() {
        return Stream.<Arguments>builder()
                .add(Arguments.of("nagyesta/lowkey-vault:2.7.1", "amd64", false))
                .add(Arguments.of("nagyesta/lowkey-vault:2.7.1", "arm64", true))
                .add(Arguments.of("nagyesta/lowkey-vault:2.7.1-ubi9-minimal", "amd64", false))
                .add(Arguments.of("lowkey-vault:2.7.1", "amd64", false))
                .add(Arguments.of("lowkey-vault:2.7.1", "arm64", false))
                .build();
    }

    @ParameterizedTest
    @MethodSource("imageRecommendationInputProvider")
    void testRecommendMultiArchImageIfApplicableShouldPrintRecommendationWhenHostIsNotMatchingImageArch(
            final String imageName, final String hostArch, final boolean logExpected
    ) {
        //given
        final var dockerImageName = DockerImageName.parse(imageName)
                .asCompatibleSubstituteFor(LowkeyVaultContainer.DEFAULT_IMAGE_NAME);
        final var underTest = spy(LowkeyVaultContainerBuilder
                .lowkeyVault(LowkeyVaultContainer.DEFAULT_IMAGE_NAME.withTag("2.7.1"))
                .build())
                .withImagePullPolicy(shouldPull -> false);
        final var loggerMock = mock(Logger.class);

        //when
        underTest.recommendMultiArchImageIfApplicable(loggerMock, dockerImageName, hostArch);

        //then
        if (logExpected) {
            verify(loggerMock).warn(
                    "An amd64 image is detected with non-amd64 ({}) host.", hostArch);
            verify(loggerMock).warn(
                    "Please consider using a multi-arch image, like: {}-ubi9-minimal",
                    StringUtils.substringAfterLast(imageName, ":"));
            verify(loggerMock).warn(
                    "See more information: https://github.com/nagyesta/lowkey-vault/tree/main/lowkey-vault-docker#arm-builds");
        }
        verifyNoMoreInteractions(loggerMock);
    }
}
