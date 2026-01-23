package ru.panyukovnn.keycloaksecuritystarter.resolver.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.panyukovnn.keycloaksecuritystarter.property.KeycloakClientProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit-тесты для {@link UrlBasedClientResolver}.
 */
class UrlBasedClientResolverTest {

    private UrlBasedClientResolver resolver;

    private KeycloakClientProperties properties;

    @BeforeEach
    void setUp() {
        properties = new KeycloakClientProperties();
        properties.setDefaultRegistration("default-client");

        List<KeycloakClientProperties.UrlMapping> mappings = new ArrayList<>();

        KeycloakClientProperties.UrlMapping serviceAMapping = new KeycloakClientProperties.UrlMapping();
        serviceAMapping.setRegistration("service-a-client");
        List<String> serviceAPatterns = new ArrayList<>();
        serviceAPatterns.add("https://api.service-a.com/**");
        serviceAMapping.setPatterns(serviceAPatterns);
        mappings.add(serviceAMapping);

        KeycloakClientProperties.UrlMapping serviceBMapping = new KeycloakClientProperties.UrlMapping();
        serviceBMapping.setRegistration("service-b-client");
        List<String> serviceBPatterns = new ArrayList<>();
        serviceBPatterns.add("https://api.service-b.com/**");
        serviceBMapping.setPatterns(serviceBPatterns);
        mappings.add(serviceBMapping);

        properties.setUrlMappings(mappings);

        resolver = new UrlBasedClientResolver(properties);
    }

    @Test
    void shouldResolveClientByMatchingPattern() {
        Optional<String> result = resolver.resolve("https://api.service-a.com/api/users");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("service-a-client");
    }

    @Test
    void shouldReturnDefaultClientWhenNoPatternMatches() {
        Optional<String> result = resolver.resolve("https://unknown-service.com/api/data");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("default-client");
    }

    @Test
    void shouldReturnEmptyWhenNoPatternMatchesAndNoDefaultClient() {
        properties.setDefaultRegistration(null);
        resolver = new UrlBasedClientResolver(properties);

        Optional<String> result = resolver.resolve("https://unknown-service.com/api/data");

        assertThat(result).isEmpty();
    }
}
