package com.github.nagyesta.lowkeyvault;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * Main class.
 */
@SuppressWarnings("checkstyle:HideUtilityClassConstructor")
@SpringBootApplication
@EnableWebMvc
public class LowkeyVaultApp {

    public static void main(final String[] args) {
        SpringApplication.run(LowkeyVaultApp.class, args);
    }
}
