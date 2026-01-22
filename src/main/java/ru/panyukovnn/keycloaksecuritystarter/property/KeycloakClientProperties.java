package ru.panyukovnn.keycloaksecuritystarter.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.AntPathMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Свойства конфигурации для Keycloak OAuth2 клиентов.
 */
@ConfigurationProperties(prefix = "keycloak-starter")
public class KeycloakClientProperties {

    /**
     * ID регистрации OAuth2 клиента по умолчанию.
     */
    private String defaultRegistration;

    /**
     * Список маппингов URL паттернов на регистрации клиентов.
     */
    private List<UrlMapping> urlMappings = new ArrayList<>();

    public String getDefaultRegistration() {
        return defaultRegistration;
    }

    public void setDefaultRegistration(String defaultRegistration) {
        this.defaultRegistration = defaultRegistration;
    }

    public List<UrlMapping> getUrlMappings() {
        return urlMappings;
    }

    public void setUrlMappings(List<UrlMapping> urlMappings) {
        this.urlMappings = urlMappings;
    }

    /**
     * Маппинг URL паттернов на OAuth2 регистрацию клиента.
     */
    public static class UrlMapping {

        /**
         * ID регистрации OAuth2 клиента.
         */
        private String registration;

        /**
         * Список URL паттернов (поддерживает Ant-style patterns).
         */
        private List<AntPathMatcher> patterns = new ArrayList<>();

        public String getRegistration() {
            return registration;
        }

        public void setRegistration(String registration) {
            this.registration = registration;
        }

        public List<AntPathMatcher> getPatterns() {
            return patterns;
        }

        public void setPatterns(List<AntPathMatcher> patterns) {
            this.patterns = patterns;
        }
    }
}
