/*
 *   MIT License
 *
 *   Copyright (c) 2021 EPAM Systems
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *   SOFTWARE.
 */

package com.epam.blast.config;

import lombok.extern.slf4j.Slf4j;
import nz.net.ultraq.thymeleaf.LayoutDialect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
public class ManagerConfiguration {

    private static final String DEFAULT_COMMAND_TEMPLATES = "commands/templates/";
    private static final String ENCODING = "UTF-8";

    @Bean
    public ExecutorService createExecutorService(
            @Value("${blast-wrapper.task-status-checking.thread-amount}") Integer threadsAmount) {
        return Executors.newFixedThreadPool(threadsAmount);
    }

    @Bean
    public TemplateEngine templateEngine(
            @Value("${blast-wrapper.template.command.dir:}") String templateCommandDir) {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addDialect(new LayoutDialect());

        final AbstractConfigurableTemplateResolver templateResolver;
        if (StringUtils.isNotBlank(templateCommandDir)
                && Files.isDirectory(Path.of(templateCommandDir))) {
            log.info("blast-wrapper.template.command.dir is '" + templateCommandDir + "'");
            templateResolver = new FileTemplateResolver();
            templateResolver.setPrefix(templateCommandDir);
        } else {
            log.warn("blast-wrapper.template.command.dir is not configured, "
                    + "default command templates will be used");
            templateResolver = new ClassLoaderTemplateResolver();
            templateResolver.setPrefix(DEFAULT_COMMAND_TEMPLATES);
        }
        templateResolver.setCharacterEncoding(ENCODING);
        templateResolver.setCacheable(false);
        templateResolver.setCheckExistence(true);
        templateResolver.setTemplateMode(TemplateMode.TEXT);
        templateEngine.addTemplateResolver(templateResolver);
        return templateEngine;
    }
}
