package com.github.nagyesta.lowkeyvault;

import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Main class.
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
@EnableWebMvc
public class LowkeyVaultApp {

    @Value("${app.token.port}")
    private int tokenPort;

    public static void main(final String[] args) {
        SpringApplication.run(LowkeyVaultApp.class, args);
    }

    @Bean
    public ServletWebServerFactory servletContainer() {
        final var tomcat = new TomcatServletWebServerFactory();
        tomcat.addAdditionalTomcatConnectors(createTokenConnector());
        return tomcat;
    }

    private Connector createTokenConnector() {
        final var connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        final var protocol = (Http11NioProtocol) connector.getProtocolHandler();
        connector.setScheme("http");
        connector.setSecure(false);
        connector.setPort(tokenPort);
        protocol.setSSLEnabled(false);
        return connector;
    }
}
