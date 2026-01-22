package ru.panyukovnn.keycloaksecuritystarter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.panyukovnn.keycloaksecuritystarter.property.KeycloakClientProperties;

/**
 * Автоконфигурация для Keycloak OAuth2 клиента.
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "keycloak-starter", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KeycloakClientProperties.class)
public class KeycloakClientSecurityStarterAutoConfiguration {
}
