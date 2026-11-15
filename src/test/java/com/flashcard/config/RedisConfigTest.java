package com.flashcard.config;

import com.flashcard.model.FlashcardJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.nio.charset.StandardCharsets;

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
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer()).isInstanceOf(GenericJacksonJsonRedisSerializer.class);
    }

    @Test
    @DisplayName("redisTemplate should store readable string keys and JSON job values")
    void testReadableSerialization() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
        RedisTemplate<String, Object> template = new RedisConfig().redisTemplate(connectionFactory);
        FlashcardJob job = FlashcardJob.builder()
                .id("job-123")
                .status("FAILED")
                .fileName("slides.pptx")
                .progress(1)
                .error("Generation failed")
                .build();

        byte[] serializedKey = ((StringRedisSerializer) template.getKeySerializer())
                .serialize("job:job-123");
        byte[] serializedValue = ((GenericJacksonJsonRedisSerializer) template.getValueSerializer())
                .serialize(job);

        assertThat(new String(serializedKey, StandardCharsets.UTF_8)).isEqualTo("job:job-123");
        assertThat(new String(serializedValue, StandardCharsets.UTF_8))
                .contains("\"id\":\"job-123\"")
                .contains("\"status\":\"FAILED\"")
                .contains("\"error\":\"Generation failed\"");
    }
}
