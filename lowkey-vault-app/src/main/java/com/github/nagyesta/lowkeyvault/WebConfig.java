package com.github.nagyesta.lowkeyvault;

import com.github.nagyesta.lowkeyvault.context.OptionalQueryApiVersionResolver;
import com.github.nagyesta.lowkeyvault.model.common.ApiVersion;
import org.springframework.stereotype.Component;
import org.springframework.web.accept.ApiVersionParser;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(final ApiVersionConfigurer configurer) {
        configurer.useVersionResolver(new OptionalQueryApiVersionResolver())
                .addSupportedVersions(ApiVersion.allVersionsAsString())
                .setDefaultVersion(ApiVersion.latest().getValue())
                .setVersionParser((ApiVersionParser<ApiVersion>) ApiVersion::parse);
    }

}
