package com.github.nagyesta.lowkeyvault.controller.common;

import com.github.nagyesta.lowkeyvault.controller.PingController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class PingControllerTest {

    @Test
    void testPingShouldReturnPongWhenCalled() {
        //given
        final PingController underTest = new PingController();

        //when
        final ResponseEntity<String> actual = underTest.ping();

        //then
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(HttpStatus.OK, actual.getStatusCode());
        Assertions.assertEquals("pong", actual.getBody());
    }
}
