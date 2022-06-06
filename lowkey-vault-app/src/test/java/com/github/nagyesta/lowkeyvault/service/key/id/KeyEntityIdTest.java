package com.github.nagyesta.lowkeyvault.service.key.id;

import com.github.nagyesta.lowkeyvault.TestConstantsKeys;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;

class KeyEntityIdTest {

    @Test
    void testAsRotationPolicyUriShouldReturnRotationPolicyUriWhenCalled() {
        //given
        final KeyEntityId underTest = TestConstantsKeys.UNVERSIONED_KEY_ENTITY_ID_1;

        //when
        final URI actual = underTest.asRotationPolicyUri();

        //then
        Assertions.assertEquals(underTest.asUri("rotationpolicy"), actual);
    }
}
