package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.model.TokenResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;

@Slf4j
@RestController
public class MetadataController {

    private final String tokenRealm;
    private final byte[] keyStoreContent;
    private final String keyStorePassword;

    public MetadataController(
            @NonNull @Value("${LOWKEY_TOKEN_REALM:assumed-identity}") final String tokenRealm,
            @NonNull @Value("${default-keystore-resource}") final String keyStoreResource,
            @NonNull @Value("${default-keystore-password}") final String keyStorePassword) throws IOException {
        this.tokenRealm = tokenRealm;
        this.keyStoreContent = new ClassPathResource(keyStoreResource).getContentAsByteArray();
        this.keyStorePassword = keyStorePassword;
    }

    @GetMapping(value = {"/metadata/identity/oauth2/token", "/metadata/identity/oauth2/token/"})
    public ResponseEntity<TokenResponse> getManagedIdentityToken(@RequestParam("resource") final URI resource) {
        final var body = new TokenResponse(resource);
        log.info("Returning token: {}", body);
        return ResponseEntity.ok()
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=" + tokenRealm)
                .body(body);
    }

    @GetMapping(value = "/metadata/default-cert/lowkey-vault.p12",
            produces = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)
    public @ResponseBody byte[] getDefaultCertificateStoreContent() {
        log.info("Returning default certificate store.");
        return keyStoreContent;
    }

    @GetMapping(value = "/metadata/default-cert/password",
            produces = MimeTypeUtils.TEXT_PLAIN_VALUE)
    public @ResponseBody String getDefaultCertificateStorePassword() {
        log.info("Returning default certificate store password.");
        return keyStorePassword;
    }
}
