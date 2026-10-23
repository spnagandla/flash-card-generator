package com.flashcard.service;

import com.flashcard.model.FlashcardJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Service to manage the lifecycle of Flashcard generation jobs using Redis.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "job:";
    private static final Duration JOB_TTL = Duration.ofHours(24);

    /**
     * Creates a new job record in Redis with a PENDING status.
     */
    public String createJob(String fileName) {
        String jobId = UUID.randomUUID().toString();
        
        FlashcardJob job = FlashcardJob.builder()
                .id(jobId)
                .fileName(fileName)
                .status("PENDING")
                .progress(0)
                .build();

        log.info("Creating new job in Redis: {} for file: {}", jobId, fileName);
        saveWithTtl(job);
        
        return jobId;
    }

    /**
     * Updates an existing job record in Redis.
     */
    public void updateJob(FlashcardJob job) {
        log.debug("Updating job status in Redis: {} to {}", job.getId(), job.getStatus());
        saveWithTtl(job);
    }

    /**
     * Retrieves a job record from Redis by its ID.
     * 
     * @param jobId The unique ID of the job.
     * @return The FlashcardJob object, or null if not found.
     */
    public FlashcardJob getJob(String jobId) {
        log.debug("Retrieving job from Redis: {}", jobId);
        Object job = redisTemplate.opsForValue().get(KEY_PREFIX + jobId);
        
        if (job == null) {
            log.warn("Job not found in Redis: {}", jobId);
            return null;
        }
        
        return (FlashcardJob) job;
    }

    private void saveWithTtl(FlashcardJob job) {
        redisTemplate.opsForValue().set(KEY_PREFIX + job.getId(), job, JOB_TTL);
    }
}
