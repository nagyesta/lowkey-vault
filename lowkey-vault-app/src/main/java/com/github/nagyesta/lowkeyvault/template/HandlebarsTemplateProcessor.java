package com.github.nagyesta.lowkeyvault.template;

import java.io.IOException;

/**
 * Interface for processing Handlebars templates.
 *
 * @param <T> The context object.
 */
public interface HandlebarsTemplateProcessor<T> {

    String processTemplate(String templateAsString, T context) throws IOException;
}
