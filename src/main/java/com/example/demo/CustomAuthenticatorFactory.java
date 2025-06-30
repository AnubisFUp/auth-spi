package com.example.demo;

import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public class CustomAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "custom-external-api-authenticator";

    @Override
    public String getDisplayType() {
        return "Custom External API Authenticator";
    }

    @Override
    public String getReferenceCategory() {
        return "password";
    }

    @Override
    public boolean isConfigurable() {
        return false;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return new AuthenticationExecutionModel.Requirement[] {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
        };
    }

    @Override
    public boolean isUserSetupAllowed() {
        return true;
    }

    @Override
    public String getHelpText() {
        return "Authenticates users against external API and creates federated users";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return null;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return new CustomAuthenticator();
    }

    @Override
    public void init(Config.Scope config) {
        // Инициализация если нужна
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Пост-инициализация если нужна
    }

    @Override
    public void close() {
        // Закрытие ресурсов если нужно
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
