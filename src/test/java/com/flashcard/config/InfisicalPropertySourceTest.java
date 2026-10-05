package com.flashcard.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class InfisicalPropertySourceTest {

    @Test
    @DisplayName("initialize should gracefully skip when Infisical environment variables are missing")
    void initialize_MissingEnvVars_SkipsGracefully() {
        InfisicalPropertySource propertySource = new InfisicalPropertySource();
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);

        assertThatCode(() -> propertySource.initialize(context))
                .doesNotThrowAnyException();

        // In absence of environment variables, environment is not modified
        verifyNoInteractions(context);
    }
}
