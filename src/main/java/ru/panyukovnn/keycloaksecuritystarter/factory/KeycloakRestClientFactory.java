package ru.panyukovnn.keycloaksecuritystarter.factory;

import org.springframework.web.client.RestClient;

/**
 * Фабрика для конфигурирования RestClient с OAuth2 интерсепторами.
 */
public interface KeycloakRestClientFactory {

    /**
     * Конфигурирует существующий RestClient.Builder, добавляя OAuth2 интерсептор
     * для указанной регистрации клиента.
     *
     * @param builder существующий builder с общими конфигурациями
     * @param registrationId ID регистрации OAuth2 клиента
     * @return тот же builder с добавленным интерсептором
     */
    RestClient.Builder configure(RestClient.Builder builder, String registrationId);
}