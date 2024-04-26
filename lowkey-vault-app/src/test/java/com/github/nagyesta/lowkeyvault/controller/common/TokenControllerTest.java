package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.ManagedIdentityTokenController;
import com.github.nagyesta.lowkeyvault.model.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;

class TokenControllerTest {

    @Test
    void testGetShouldReturnTokenWhenCalled() {
        //given
        final ManagedIdentityTokenController underTest = new ManagedIdentityTokenController();
        final URI resource = URI.create("https://localhost:8443/");

        //when
        final ResponseEntity<TokenResponse> actual = underTest.get(resource);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(resource, actual.getBody().resource());
        Assertions.assertEquals("dummy", actual.getBody().accessToken());
    }
}
