package com.flashcard.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class RedisConfigTest {

    @Test
    @DisplayName("redisTemplate bean method should configure RedisTemplate with connection factory")
    void testRedisTemplateBean() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisConfig config = new RedisConfig();

        RedisTemplate<String, Object> template = config.redisTemplate(connectionFactory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isSameAs(connectionFactory);
    }
}
