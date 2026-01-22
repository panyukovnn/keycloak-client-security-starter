package ru.panyukovnn.keycloaksecuritystarter.resolver;

import java.util.Optional;

/**
 * Интерфейс для определения OAuth2 регистрации клиента на основе URL запроса.
 */
public interface ClientRegistrationResolver {

    /**
     * Определяет ID регистрации OAuth2 клиента для указанного URL.
     *
     * @param url URL исходящего запроса
     * @return Optional с ID регистрации клиента, если найден
     */
    Optional<String> resolve(String url);
}