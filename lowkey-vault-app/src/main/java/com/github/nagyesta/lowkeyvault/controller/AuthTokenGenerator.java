package com.github.nagyesta.lowkeyvault.controller;

import com.github.nagyesta.lowkeyvault.model.v7_2.key.constants.KeyType;
import com.github.nagyesta.lowkeyvault.service.key.util.KeyGenUtil;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;

@Component
@Slf4j
@Getter
public class AuthTokenGenerator {

    private final KeyPair keyPair;
    private final String issuer;

    public AuthTokenGenerator(@Value("${LOWKEY_TOKEN_ISSUER}") final String issuer) {
        this.issuer = issuer;
        this.keyPair = KeyGenUtil.generateRsa(KeyType.RSA.getValidKeyParameters(Integer.class).first(), null);
    }

    public String generateToken(
            final URI audience,
            final Instant issuedAt,
            final Instant expires) {
        return Jwts.builder()
                .header()
                .type("JWT")
                .and()
                .claims()
                .issuer(issuer)
                .audience().add(audience.toString())
                .and()
                .expiration(Date.from(expires))
                .notBefore(Date.from(issuedAt))
                .issuedAt(Date.from(issuedAt))
                .and()
                .signWith(keyPair.getPrivate())
                .compact();
    }

}
