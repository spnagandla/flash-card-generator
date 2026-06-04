package com.flashcard.config;

import com.infisical.sdk.InfisicalSdk;
import com.infisical.sdk.config.SdkConfig;
import com.infisical.sdk.models.Secret;
import com.infisical.sdk.util.InfisicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InfisicalPropertySource implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(InfisicalPropertySource.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("Loading secrets from Infisical...");

        try {
            String clientId = System.getenv("INFISICAL_CLIENT_ID");
            String clientSecret = System.getenv("INFISICAL_CLIENT_SECRET");
            String projectId = System.getenv("INFISICAL_PROJECT_ID");

            if (isBlank(clientId) || isBlank(clientSecret) || isBlank(projectId)) {
                log.warn("Infisical credentials are not configured. Skipping secret loading.");
                return;
            }

            var sdk = new InfisicalSdk(new SdkConfig.Builder().build());
            sdk.Auth().UniversalAuthLogin(clientId, clientSecret);

            List<Secret> secrets = sdk.Secrets().ListSecrets(
                    projectId,
                    "dev",
                    "/",
                    false,
                    true,
                    true
            );

            Map<String, Object> secretMap = new HashMap<>();
            for (Secret secret : secrets) {
                String key = secret.getSecretKey();
                String value = secret.getSecretValue();

                if (key.equals("GEMINI_API_KEY")) {
                    secretMap.put("GEMINI_API_KEY", value);
                    secretMap.put("gemini.api.key", value);
                }

                if (key.equals("FIRE_BASE_SERVICE_ACCOUNT_JSON")) {
                    secretMap.put("FIRE_BASE_SERVICE_ACCOUNT_JSON", value);
                }

                if (key.equals("REDIS_URL")) {
                    secretMap.put("REDIS_URL", value);
                    secretMap.put("spring.data.redis.url", value);
                }
            }

            applicationContext.getEnvironment()
                    .getPropertySources()
                    .addFirst(new MapPropertySource("infisical", secretMap));

            log.info("Loaded {} app secrets from Infisical.", secretMap.size());
        } catch (InfisicalException e) {
            throw new RuntimeException("Infisical auth/fetch failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error loading Infisical secrets: " + e.getMessage(), e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
