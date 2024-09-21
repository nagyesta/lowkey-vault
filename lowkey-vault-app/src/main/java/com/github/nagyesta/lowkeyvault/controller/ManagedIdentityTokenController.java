package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.model.TokenResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
public class ManagedIdentityTokenController {

    private final String tokenRealm;

    public ManagedIdentityTokenController(
            @NonNull @Value("${LOWKEY_TOKEN_REALM:assumed-identity}") final String tokenRealm) {
        this.tokenRealm = tokenRealm;
    }

    @GetMapping(value = {"/metadata/identity/oauth2/token", "/metadata/identity/oauth2/token/"})
    public ResponseEntity<TokenResponse> get(@RequestParam("resource") final URI resource) {
        final TokenResponse body = new TokenResponse(resource);
        log.info("Returning token: {}", body);
        return ResponseEntity.ok()
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=" + tokenRealm)
                .body(body);
    }
}
