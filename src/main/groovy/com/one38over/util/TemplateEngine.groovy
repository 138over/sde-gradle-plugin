package com.one38over.util

import groovy.util.slurpersupport.NodeChild
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.FileTemplateResolver
import groovy.util.logging.Slf4j

@Slf4j
class TemplateEngine {
    static String run(String template, String suffix, String mode, String directory, String output, Map config) {
        FileTemplateResolver resolver = new FileTemplateResolver()
        resolver.setTemplateMode(mode.toUpperCase());
        resolver.setSuffix(suffix);
        resolver.setPrefix(directory);

        Locale locale = Locale.getDefault();
        org.thymeleaf.TemplateEngine templateEngine = new org.thymeleaf.TemplateEngine()
        templateEngine.setTemplateResolver(resolver);
        Context context = new Context(locale.US, config)
        FileWriter fw = new FileWriter(output);
        templateEngine.process(template, context, fw)
        fw.close()
        return new File(output).text
    }
}
