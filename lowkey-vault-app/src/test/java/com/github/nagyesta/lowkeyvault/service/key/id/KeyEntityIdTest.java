package com.github.nagyesta.lowkeyvault.service.key.id;

import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.github.nagyesta.lowkeyvault.TestConstantsUri.HTTPS_LOCALHOST_8443;

class KeyEntityIdTest {

    @Test
    void testAsRotationPolicyUriShouldReturnRotationPolicyUriWhenCalled() {
        //given
        final var underTest = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        //when
        final var actual = underTest.asRotationPolicyUri(HTTPS_LOCALHOST_8443);

        //then
        Assertions.assertEquals(underTest.asUri(HTTPS_LOCALHOST_8443, "rotationpolicy"), actual);
    }
}
