package com.flashcard.service;

import com.flashcard.model.FlashcardJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private JobService jobService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        jobService = new JobService(redisTemplate);
    }

    @Test
    @DisplayName("createJob should generate UUID, store PENDING job in Redis, and return jobId")
    void createJob_Success() {
        String fileName = "notes.pdf";

        String jobId = jobService.createJob(fileName);

        assertThat(jobId).isNotBlank();
        ArgumentCaptor<FlashcardJob> jobCaptor = ArgumentCaptor.forClass(FlashcardJob.class);
        verify(valueOperations, times(1)).set(eq("job:" + jobId), jobCaptor.capture());

        FlashcardJob capturedJob = jobCaptor.getValue();
        assertThat(capturedJob.getId()).isEqualTo(jobId);
        assertThat(capturedJob.getFileName()).isEqualTo(fileName);
        assertThat(capturedJob.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("updateJob should save updated job in Redis")
    void updateJob_Success() {
        FlashcardJob job = FlashcardJob.builder()
                .id("job-456")
                .fileName("deck.docx")
                .status("PROCESSING")
                .build();

        jobService.updateJob(job);

        verify(valueOperations, times(1)).set("job:job-456", job);
    }

    @Test
    @DisplayName("getJob should return FlashcardJob when key exists in Redis")
    void getJob_Found() {
        FlashcardJob existingJob = FlashcardJob.builder()
                .id("job-789")
                .fileName("slides.pptx")
                .status("COMPLETED")
                .build();

        when(valueOperations.get("job:job-789")).thenReturn(existingJob);

        FlashcardJob result = jobService.getJob("job-789");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("job-789");
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(valueOperations, times(1)).get("job:job-789");
    }

    @Test
    @DisplayName("getJob should return null when key is missing in Redis")
    void getJob_NotFound() {
        when(valueOperations.get("job:missing")).thenReturn(null);

        FlashcardJob result = jobService.getJob("missing");

        assertThat(result).isNull();
        verify(valueOperations, times(1)).get("job:missing");
    }
}
