package com.flashcard.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonCompatibilityConfigTest {

    @Test
    @DisplayName("objectMapper bean method should return a new non-null ObjectMapper instance")
    void testObjectMapperBean() {
        JacksonCompatibilityConfig config = new JacksonCompatibilityConfig();
        ObjectMapper mapper = config.objectMapper();

        assertThat(mapper).isNotNull();
    }
}
