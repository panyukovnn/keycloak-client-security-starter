package ru.panyukovnn.keycloaksecuritystarter.factory.impl;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestClient;
import ru.panyukovnn.keycloaksecuritystarter.factory.KeycloakRestClientFactory;
import ru.panyukovnn.keycloaksecuritystarter.interceptor.KeycloakOAuth2Interceptor;

/**
 * Реализация фабрики для конфигурирования RestClient с OAuth2 интерсепторами.
 */
public class KeycloakRestClientFactoryImpl implements KeycloakRestClientFactory {

    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public KeycloakRestClientFactoryImpl(OAuth2AuthorizedClientManager authorizedClientManager) {
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public RestClient.Builder configure(RestClient.Builder builder, String registrationId) {
        KeycloakOAuth2Interceptor interceptor = new KeycloakOAuth2Interceptor(registrationId, authorizedClientManager);

        return builder.requestInterceptor(interceptor);
    }
}
