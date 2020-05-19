package ru.iteco.attach.config;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {
    private final ServerProperties properties;

    public WebFluxConfig(final ServerProperties serverProperties) {
        this.properties = serverProperties;
    }

    @Override
    public void configurePathMatching(PathMatchConfigurer configurer) {
        configurer
                .setUseCaseSensitiveMatch(true)
                .setUseTrailingSlashMatch(false)
                .addPathPrefix(properties.getServlet().getContextPath(), HandlerTypePredicate.forAnyHandlerType());
    }
}
