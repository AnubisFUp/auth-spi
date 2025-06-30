package com.example.demo;

import java.io.IOException;

import org.jboss.logging.Logger;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UsersApiLegacyService {

    private final KeycloakSession session;
    private static final Logger LOG = Logger.getLogger(UsersApiLegacyService.class);
    private static final String API_DOMAIN = "https://idev.etm.ru";

    public UsersApiLegacyService(KeycloakSession session) {
        this.session = session;
    }

    /**
     * Аутентификация пользователя и получение его данных в одном запросе.
     * Вызывает /users/login, который возвращает данные пользователя при успешной аутентификации.
     */
    public User authenticateUser(String username, String password) {
        LOG.infof("Начинается аутентификация пользователя: %s (%s)", username, password);

        SimpleHttp simpleHttp = SimpleHttp.doPost(API_DOMAIN + "/api/ipro/user/login", this.session)
            .param("log", username)
            .param("pwd", password)
            .header("accept", "application/json");

        try {
            LOG.info("Отправка POST-запроса на " + API_DOMAIN + "/api/ipro/user/login");
            String response = simpleHttp.asString();
            LOG.info("Ответ от API: " + response);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            if (rootNode == null) {
                LOG.warn("Пустой JSON в ответе от API");
                return null;
            }

            JsonNode statusNode = rootNode.get("status");
            if (statusNode == null) {
                LOG.warn("Отсутствует поле 'status' в ответе");
                return null;
            }

            int code = statusNode.has("code") ? statusNode.get("code").asInt() : -1;
            LOG.infof("Код статуса из ответа: %d", code);

            if (code == 200) {
                JsonNode dataNode = rootNode.get("data");
                if (dataNode != null) {
                    LOG.info("Извлекаются данные пользователя из ответа");

                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(dataNode.has("email") ? dataNode.get("email").asText() : null);
                    user.setFirstName(dataNode.has("name") ? dataNode.get("name").asText() : null);
                    user.setLastName(dataNode.has("name") ? dataNode.get("name").asText() : null);

                    LOG.infof("Аутентификация успешна. Пользователь: %s, email: %s", user.getUsername(), user.getEmail());
                    return user;
                } else {
                    LOG.warn("Поле 'data' отсутствует в ответе при успешном статусе");
                }
            } else {
                LOG.warnf("Аутентификация не удалась. Код: %d", code);
            }

        } catch (IOException e) {
            LOG.error("Ошибка при аутентификации пользователя " + username + ": " + e.getMessage(), e);
        }

        LOG.info("Аутентификация завершена неуспешно");
        return null;
    }
}

