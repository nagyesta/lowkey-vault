package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.ManagedIdentityTokenController;
import com.github.nagyesta.lowkeyvault.model.TokenResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

class TokenControllerTest {

    @Test
    void testGetShouldReturnTokenWhenCalled() {
        //given
        final String tokenRealm = "realm-name";
        final ManagedIdentityTokenController underTest = new ManagedIdentityTokenController(tokenRealm);
        final URI resource = URI.create("https://localhost:8443/");

        //when
        final ResponseEntity<TokenResponse> actual = underTest.get(resource);

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertNotNull(actual.getBody());
        Assertions.assertEquals(resource, actual.getBody().resource());
        Assertions.assertEquals("dummy", actual.getBody().accessToken());
        Assertions.assertEquals(List.of("Basic realm=" + tokenRealm), actual.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE));
    }

    @Test
    void testConstructorShouldThrowExceptionWhenCalledWithNull() {
        //given

        //when
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ManagedIdentityTokenController(null));

        //then + exception
    }
}
