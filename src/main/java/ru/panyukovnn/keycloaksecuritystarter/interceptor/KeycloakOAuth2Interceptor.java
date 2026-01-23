package ru.panyukovnn.keycloaksecuritystarter.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;

/**
 * Интерсептор для автоматического добавления OAuth2 access token к исходящим HTTP запросам.
 */
public class KeycloakOAuth2Interceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakOAuth2Interceptor.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final String registrationId;
    private final OAuth2AuthorizedClientManager authorizedClientManager;

    public KeycloakOAuth2Interceptor(String registrationId, OAuth2AuthorizedClientManager authorizedClientManager) {
        this.registrationId = registrationId;
        this.authorizedClientManager = authorizedClientManager;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        OAuth2AuthorizedClient authorizedClient = authorizeClient();

        if (authorizedClient == null) {
            LOG.warn("Не удалось получить OAuth2 авторизованного клиента для регистрации: {}", registrationId);

            return execution.execute(request, body);
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        request.getHeaders().add(AUTHORIZATION_HEADER, BEARER_PREFIX + accessToken);

        LOG.debug("Добавлен OAuth2 токен для регистрации '{}' к запросу: {} {}", registrationId, request.getMethod(), request.getURI());

        return execution.execute(request, body);
    }

    private OAuth2AuthorizedClient authorizeClient() {
        OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(registrationId)
                .principal(registrationId)
                .build();

        return authorizedClientManager.authorize(authorizeRequest);
    }
}