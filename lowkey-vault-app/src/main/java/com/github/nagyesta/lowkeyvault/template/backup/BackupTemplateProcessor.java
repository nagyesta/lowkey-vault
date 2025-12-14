package com.github.nagyesta.lowkeyvault.template.backup;

import com.github.jknack.handlebars.Handlebars;
import com.github.nagyesta.lowkeyvault.template.HandlebarsTemplateProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BackupTemplateProcessor
        implements HandlebarsTemplateProcessor<BackupContext> {

    private final TimeHelperSource timeHelperSource;

    @Autowired
    public BackupTemplateProcessor(final TimeHelperSource timeHelperSource) {
        this.timeHelperSource = timeHelperSource;
    }

    @Override
    public String processTemplate(
            final String templateAsString,
            final BackupContext context) throws IOException {
        final var handlebars = new Handlebars();
        handlebars.registerHelpers(timeHelperSource);
        final var template = handlebars.compileInline(templateAsString);
        return template.apply(context);
    }
}
