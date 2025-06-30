package com.example.demo;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.jboss.logging.Logger;

import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import java.io.IOException;

public class CustomAuthenticator implements Authenticator {

    private static final Logger LOG = Logger.getLogger(CustomAuthenticator.class);

    protected boolean hasSessionCookie(AuthenticationFlowContext context) {
        Cookie sessionCookie = context.getHttpRequest().getHttpHeaders().getCookies().get("KEYCLOAK_SESSION");
        boolean result = sessionCookie != null;
        if (result) {
            LOG.info("Bypassing custom authenticator because KEYCLOAK_SESSION cookie is present");
        } else {
            LOG.info("KEYCLOAK_SESSION cookie not found, showing authentication form");
        }
        return result;
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        LOG.info("Custom Authenticator - authenticate");

        if (hasSessionCookie(context)) {
            context.success();
            return;
        }

        Response challenge = context.form().createForm("custom-auth.ftl");
        context.challenge(challenge);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        LOG.info("Custom Authenticator - action");

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = formData.getFirst("username");
        String password = formData.getFirst("password");

        LOG.debugf("Получены данные формы: username=%s, password=%s", username, password != null ? "***" : "null");

        if (username == null || password == null) {
            LOG.warn("Не переданы username или password в форме");
            Response challenge = context.form()
                    .setError("missingCredentials", "Необходимо указать имя пользователя и пароль")
                    .createForm("custom-auth.ftl");
            context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            return;
        }

        try {
            User user = authenticateAndGetUser(username, password, context.getSession());
            if (user != null) {
                RealmModel realm = context.getRealm();
                UserModel userModel = context.getSession().users().getUserByUsername(realm, username);
                if (userModel == null) {
                    userModel = context.getSession().users().addUser(realm, username);
                    userModel.setEnabled(true);
                }
                userModel.setEmail(user.getEmail());
                userModel.setFirstName(user.getFirstName());
                userModel.setLastName(user.getLastName());
                context.setUser(userModel);
                context.success();
            } else {
                Response challenge = context.form()
                        .setError("invalidCredentials", "Неверные учетные данные")
                        .createForm("custom-auth.ftl");
                context.failureChallenge(AuthenticationFlowError.INVALID_CREDENTIALS, challenge);
            }
        } catch (IOException e) {
            LOG.error("Ошибка при вызове внешнего API", e);
            Response challenge = context.form()
                    .setError("internalError", "Внутренняя ошибка сервера")
                    .createForm("custom-auth.ftl");
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, challenge);
        }
    }

    private User authenticateAndGetUser(String username, String password, KeycloakSession session) throws IOException {
        UsersApiLegacyService apiService = new UsersApiLegacyService(session);
        return apiService.authenticateUser(username, password);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
    }

    @Override
    public void close() {
    }
}
