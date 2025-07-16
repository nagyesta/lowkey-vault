package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.model.KeyDetails;
import com.github.nagyesta.lowkeyvault.model.OpenIdConfiguration;
import com.github.nagyesta.lowkeyvault.model.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@RestController
public class MetadataController {

    private static final int DEFAULT_PORT = 80;

    private final String tokenRealm;
    private final byte[] keyStoreContent;
    private final String keyStorePassword;
    private final AuthTokenGenerator authTokenGenerator;

    public MetadataController(
            @NonNull @Value("${LOWKEY_TOKEN_REALM:assumed-identity}") final String tokenRealm,
            @NonNull final AuthTokenGenerator authTokenGenerator,
            @NonNull @Value("${default-keystore-resource}") final String keyStoreResource,
            @NonNull @Value("${default-keystore-password}") final String keyStorePassword) throws IOException {
        this.tokenRealm = tokenRealm;
        this.keyStoreContent = new ClassPathResource(keyStoreResource).getContentAsByteArray();
        this.keyStorePassword = keyStorePassword;
        this.authTokenGenerator = authTokenGenerator;
    }

    @GetMapping(value = "/metadata/identity/oauth2/token")
    public ResponseEntity<TokenResponse> getManagedIdentityToken(@RequestParam("resource") final URI resource) {
        final var now = Instant.now();
        final var expiration = now.plus(Duration.ofHours(1L));
        final var token = authTokenGenerator.generateToken(resource, now, expiration);
        final var body = new TokenResponse(resource, token, now, expiration);
        log.info("Returning token: {}", body);
        return ResponseEntity.ok()
                .header(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=" + tokenRealm)
                .body(body);
    }

    @GetMapping(value = "/metadata/identity/.well-known/openid-configuration")
    public ResponseEntity<OpenIdConfiguration> getOpenIdConfiguration(final HttpServletRequest request) {
        final var configuration = new OpenIdConfiguration(authTokenGenerator.getIssuer(),
                URI.create(hostBaseUrl(request) + "/metadata/identity/.well-known/openid-configuration/jwks"));
        return ResponseEntity.ok(configuration);
    }

    @GetMapping(value = "/metadata/identity/.well-known/openid-configuration/jwks")
    public ResponseEntity<KeyDetails> getJwks() {
        final var keyDetails = new KeyDetails(authTokenGenerator.getKeyPair());
        return ResponseEntity.ok(keyDetails);
    }

    @GetMapping(value = "/metadata/default-cert/lowkey-vault.p12",
            produces = MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE)
    public byte[] getDefaultCertificateStoreContent() {
        log.info("Returning default certificate store.");
        return keyStoreContent;
    }

    @GetMapping(value = "/metadata/default-cert/password",
            produces = MimeTypeUtils.TEXT_PLAIN_VALUE)
    public String getDefaultCertificateStorePassword() {
        log.info("Returning default certificate store password.");
        return keyStorePassword;
    }

    private String hostBaseUrl(final HttpServletRequest request) {
        final var port = Optional.of(request.getServerPort())
                .filter(p -> p != DEFAULT_PORT)
                .map(p -> ":" + p)
                .orElse("");
        return request.getScheme() + "://" + request.getServerName() + port;
    }
}
