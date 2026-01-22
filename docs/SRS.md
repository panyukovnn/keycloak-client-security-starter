# SRS: Keycloak OAuth2 Spring Boot Starter

## 1. Обзор существующих решений

### 1.1 Встроенные решения Spring Security

**Spring Security 6.4+** предоставляет встроенный интерсептор `OAuth2ClientHttpRequestInterceptor` для автоматического добавления OAuth2 токенов к исходящим запросам RestClient.

**spring-boot-starter-oauth2-client** — стандартный стартер, который:
- Поддерживает client_credentials grant type
- Автоматически кеширует токены через `OAuth2AuthorizedClientService`
- Позволяет конфигурировать множество клиентов через application.yml

### 1.2 Вывод

**Готового стартера, полностью решающего задачу, не существует.** Однако можно построить решение на базе:
- `spring-boot-starter-oauth2-client`
- `OAuth2ClientHttpRequestInterceptor` (Spring Security 6.4+)
- Кастомная логика выбора клиента по запросу

---

## 2. Функциональные требования

### 2.1 Основные возможности

| ID | Требование | Приоритет |
|----|------------|-----------|
| FR-01 | Автоматическое получение access token перед исходящим запросом | Must |
| FR-02 | Кеширование токенов с учетом срока их действия (TTL) | Must |
| FR-03 | Поддержка множества Keycloak клиентов и реалмов | Must |
| FR-04 | Автоматическое обновление токена при истечении срока | Must |
| FR-05 | Определение нужного клиента на основе URL запроса | Must |
| FR-06 | Возможность явного указания клиента через аннотацию/конфигурацию | Should |
| FR-07 | Поддержка client_credentials grant type | Must |
| FR-08 | Graceful degradation при недоступности Keycloak | Should |
| FR-09 | Использование стандартного RestClient.Builder для наследования общих конфигураций | Must |

### 2.2 Определение клиента для запроса

Варианты определения какой OAuth2 клиент использовать:

1. **URL-based mapping** — сопоставление URL паттернов с клиентами:
   ```yaml
   keycloak-starter:
     clients:
       service-a:
         url-patterns:
           - "https://api.service-a.com/**"
           - "https://internal.service-a.local/**"
         registration: keycloak-service-a
       service-b:
         url-patterns:
           - "https://api.service-b.com/**"
         registration: keycloak-service-b
   ```

2. **Программное указание** — через фабрику с передачей стандартного builder:
   ```java
   @Autowired
   KeycloakRestClientFactory factory;

   @Autowired
   RestClient.Builder builder;  // стандартный builder с общими настройками

   RestClient client = factory.configure(builder, "keycloak-service-a")
       .baseUrl("https://api.service-a.com")
       .build();
   ```

3. **Аннотации на Feign-клиентах** (опционально):
   ```java
   @KeycloakClient(registration = "service-a")
   @FeignClient(name = "serviceA")
   public interface ServiceAClient { ... }
   ```

---

## 3. Нефункциональные требования

| ID | Требование |
|----|------------|
| NFR-01 | Совместимость со Spring Boot 3.2+ |
| NFR-02 | Совместимость со Spring Security 6.4+ |
| NFR-03 | Поддержка Java 17+ |
| NFR-04 | Минимальные зависимости (только spring-boot-starter-oauth2-client) |
| NFR-05 | Thread-safe кеширование токенов |
| NFR-06 | Метрики через Micrometer (опционально) |
| NFR-07 | Логирование через SLF4J |

---

## 4. Архитектура

### 4.1 Структура модулей

```
keycloak-security-starter/
├── src/main/java/
│   └── com/example/keycloak/
│       ├── autoconfigure/
│       │   ├── KeycloakClientAutoConfiguration.java
│       │   └── KeycloakClientProperties.java
│       ├── interceptor/
│       │   └── KeycloakOAuth2Interceptor.java
│       ├── resolver/
│       │   ├── ClientRegistrationResolver.java
│       │   └── UrlBasedClientResolver.java
│       ├── factory/
│       │   └── KeycloakRestClientFactory.java
│       └── cache/
│           └── TokenCacheManager.java
├── src/main/resources/
│   └── META-INF/
│       └── spring/
│           └── org.springframework.boot.autoconfigure.AutoConfiguration.imports
└── build.gradle.kts
```

### 4.2 Sequence Diagram

```
User Code          RestClient       Interceptor       Resolver        AuthClientManager     Keycloak
    │                  │                │                │                   │                  │
    │──request()──────►│                │                │                   │                  │
    │                  │──intercept()──►│                │                   │                  │
    │                  │                │──resolve(url)─►│                   │                  │
    │                  │                │◄─registration──│                   │                  │
    │                  │                │                                    │                  │
    │                  │                │──authorize(registration)─────────►│                  │
    │                  │                │                                    │                  │
    │                  │                │                      [cache miss]  │──POST /token───►│
    │                  │                │                                    │◄─access_token───│
    │                  │                │                                    │                  │
    │                  │                │◄─────────────OAuth2AccessToken────│                  │
    │                  │                │                                                       │
    │                  │◄─request+auth──│                                                       │
    │◄──response───────│                                                                       │
```

---

## 5. Конфигурация

### 5.1 application.yml пример

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          # Клиент для Service A (realm: realm-a)
          keycloak-service-a:
            provider: keycloak-realm-a
            client-id: my-service-client
            client-secret: ${KEYCLOAK_SERVICE_A_SECRET}
            authorization-grant-type: client_credentials
            scope: openid

          # Клиент для Service B (realm: realm-b)
          keycloak-service-b:
            provider: keycloak-realm-b
            client-id: another-client
            client-secret: ${KEYCLOAK_SERVICE_B_SECRET}
            authorization-grant-type: client_credentials
            scope: openid

        provider:
          keycloak-realm-a:
            issuer-uri: https://keycloak.example.com/realms/realm-a
          keycloak-realm-b:
            issuer-uri: https://keycloak.example.com/realms/realm-b

# Конфигурация стартера
keycloak-starter:
  enabled: true
  default-registration: keycloak-service-a
  url-mappings:
    - registration: keycloak-service-a
      patterns:
        - "https://api.service-a.com/**"
        - "https://internal-a.local/**"
    - registration: keycloak-service-b
      patterns:
        - "https://api.service-b.com/**"
```

### 5.2 Программное использование

Фабрика принимает стандартный `RestClient.Builder`, что позволяет наследовать общие конфигурации (таймауты, логирование, дополнительные интерсепторы и т.д.):

```java
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient serviceAClient(
            RestClient.Builder builder,  // стандартный builder с общими конфигурациями
            KeycloakRestClientFactory factory) {
        return factory.configure(builder, "keycloak-service-a")
            .baseUrl("https://api.service-a.com")
            .build();
    }

    @Bean
    public RestClient serviceBClient(
            RestClient.Builder builder,
            KeycloakRestClientFactory factory) {
        return factory.configure(builder, "keycloak-service-b")
            .baseUrl("https://api.service-b.com")
            .build();
    }
}
```

Метод `configure()` добавляет OAuth2 интерсептор к существующему builder, сохраняя все предыдущие настройки:

```java
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
```

---

## 6. План реализации

### Фаза 1: Базовая структура проекта

- [x] Создать Gradle проект со структурой Spring Boot стартера
- [x] Настроить зависимости (spring-boot-starter-oauth2-client, spring-boot-autoconfigure)
- [x] Создать базовые классы конфигурации
- [x] Настроить spring.factories / AutoConfiguration.imports

### Фаза 2: Core функциональность

- [x] Реализовать `KeycloakClientProperties` для чтения конфигурации
- [x] Реализовать `ClientRegistrationResolver` интерфейс и URL-based реализацию
- [x] Реализовать `KeycloakOAuth2Interceptor` на базе `ClientHttpRequestInterceptor`
- [x] Интегрировать с `OAuth2AuthorizedClientManager` для получения/кеширования токенов

### Фаза 3: RestClient интеграция

- [x] Создать `KeycloakRestClientFactory` с методом `configure(RestClient.Builder, String registrationId)`
- [x] Обеспечить совместимость со стандартным `RestClient.Builder` (сохранение общих конфигураций)
- [x] Добавить автоконфигурацию для регистрации интерсептора
- [ ] Реализовать `RestClientCustomizer` для автоматического применения интерсептора (опционально)
- [x] Добавить логирование событий получения токена

### Фаза 4: Тестирование и документация

- [ ] Unit тесты для всех компонентов
- [ ] Integration тесты с Testcontainers (Keycloak)
- [ ] Написать README с примерами использования
- [ ] Добавить примеры конфигурации

---

## 7. Зависимости проекта

```kotlin
dependencies {
    // Основные зависимости
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    // Опциональные
    compileOnly("io.micrometer:micrometer-core")

    // Тестирование
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:junit-jupiter")
}
```

---

## 8. Риски и митигация

| Риск | Вероятность | Влияние | Митигация |
|------|-------------|---------|-----------|
| Keycloak недоступен | Средняя | Высокое | Circuit breaker, fallback, retry с exponential backoff |
| Token expiration во время запроса | Низкая | Среднее | Запрашивать токен с запасом по времени (buffer) |
| Race condition при обновлении токена | Низкая | Среднее | Использовать synchronized или ReentrantLock |
| Неверная конфигурация URL mapping | Средняя | Среднее | Валидация при старте, информативные ошибки |

---

## 9. Источники

- [Spring Security OAuth2 RestClient Interceptor (GitHub)](https://github.com/mjeffrey/spring-security-oauth2-restclient-interceptor)
- [How to integrate Spring Boot 3, Spring Security, and Keycloak (Red Hat)](https://developers.redhat.com/articles/2023/07/24/how-integrate-spring-boot-3-spring-security-and-keycloak)
- [A Quick Guide to Using Keycloak with Spring Boot (Baeldung)](https://www.baeldung.com/spring-boot-keycloak)
- [Client Credentials Flow with Spring Security (Okta)](https://developer.okta.com/blog/2021/05/05/client-credentials-spring-security)
