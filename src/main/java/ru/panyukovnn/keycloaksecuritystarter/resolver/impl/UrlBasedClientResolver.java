package ru.panyukovnn.keycloaksecuritystarter.resolver.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import ru.panyukovnn.keycloaksecuritystarter.property.KeycloakClientProperties;
import ru.panyukovnn.keycloaksecuritystarter.resolver.ClientRegistrationResolver;

import java.util.Optional;

/**
 * Реализация резолвера клиентов на основе URL паттернов.
 */
public class UrlBasedClientResolver implements ClientRegistrationResolver {

    private static final Logger LOG = LoggerFactory.getLogger(UrlBasedClientResolver.class);

    private final AntPathMatcher pathMatcher;
    private final KeycloakClientProperties properties;

    public UrlBasedClientResolver(KeycloakClientProperties properties) {
        this.properties = properties;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public Optional<String> resolve(String url) {
        for (KeycloakClientProperties.UrlMapping mapping : properties.getUrlMappings()) {
            for (String pattern : mapping.getPatterns()) {
                if (pathMatcher.match(pattern, url)) {
                    LOG.debug("URL '{}' соответствует паттерну '{}', используется регистрация: {}", url, pattern, mapping.getRegistration());

                    return Optional.of(mapping.getRegistration());
                }
            }
        }

        if (properties.getDefaultRegistration() != null) {
            LOG.debug("Для URL '{}' не найден подходящий паттерн, используется регистрация по умолчанию: {}", url, properties.getDefaultRegistration());

            return Optional.of(properties.getDefaultRegistration());
        }

        LOG.warn("Не удалось определить OAuth2 регистрацию для URL: {}", url);

        return Optional.empty();
    }
}
