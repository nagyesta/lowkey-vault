package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.context.OptionalQueryApiVersionResolver;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.ApiVersionParser;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static com.github.nagyesta.lowkeyvault.model.common.ApiConstants.*;

@Component
public class WebConfig implements WebMvcConfigurer {

    private static final ApiVersionParser<String> IDENTITY_VERSION_PARSER = version -> version;

    @Override
    public void configureApiVersioning(final ApiVersionConfigurer configurer) {
        configurer.useVersionResolver(new OptionalQueryApiVersionResolver())
                .addSupportedVersions(V_7_2, V_7_3, V_7_4, V_7_5, V_7_6)
                .setDefaultVersion(V_7_6)
                .setVersionParser(IDENTITY_VERSION_PARSER);
    }

}
