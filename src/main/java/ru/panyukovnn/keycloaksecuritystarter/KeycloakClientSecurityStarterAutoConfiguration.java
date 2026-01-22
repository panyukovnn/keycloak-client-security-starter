package ru.panyukovnn.keycloaksecuritystarter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.client.RestClient;
import ru.panyukovnn.keycloaksecuritystarter.factory.KeycloakRestClientFactory;
import ru.panyukovnn.keycloaksecuritystarter.factory.impl.KeycloakRestClientFactoryImpl;
import ru.panyukovnn.keycloaksecuritystarter.property.KeycloakClientProperties;
import ru.panyukovnn.keycloaksecuritystarter.resolver.ClientRegistrationResolver;
import ru.panyukovnn.keycloaksecuritystarter.resolver.impl.UrlBasedClientResolver;

/**
 * Автоконфигурация для Keycloak OAuth2 клиента.
 */
@AutoConfiguration
@ConditionalOnClass(RestClient.class)
@ConditionalOnProperty(prefix = "keycloak-starter", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KeycloakClientProperties.class)
public class KeycloakClientSecurityStarterAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakClientSecurityStarterAutoConfiguration.class);

    /**
     * Создает менеджер авторизованных OAuth2 клиентов для client_credentials flow.
     *
     * @param clientRegistrationRepository репозиторий регистраций клиентов
     * @param authorizedClientRepository репозиторий авторизованных клиентов
     * @return менеджер авторизованных клиентов
     */
    @Bean
    @ConditionalOnMissingBean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {
        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        DefaultOAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientRepository);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Создает резолвер OAuth2 регистраций на основе URL паттернов.
     *
     * @param properties конфигурационные свойства стартера
     * @return резолвер клиентских регистраций
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientRegistrationResolver clientRegistrationResolver(KeycloakClientProperties properties) {
        return new UrlBasedClientResolver(properties);
    }

    /**
     * Создает фабрику для конфигурирования RestClient с OAuth2 интерсепторами.
     *
     * @param authorizedClientManager менеджер авторизованных клиентов
     * @return фабрика RestClient
     */
    @Bean
    @ConditionalOnMissingBean
    public KeycloakRestClientFactory keycloakRestClientFactory(OAuth2AuthorizedClientManager authorizedClientManager) {
        return new KeycloakRestClientFactoryImpl(authorizedClientManager);
    }
}
