package com.flashcard.service;

import com.flashcard.model.FlashcardJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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

    /**
     * Creates a new job record in Redis with a PENDING status.
     */
    public String createJob(String fileName) {
        String jobId = UUID.randomUUID().toString();
        
        FlashcardJob job = FlashcardJob.builder()
                .id(jobId)
                .fileName(fileName)
                .status("PENDING")
                .build();

        log.info("Creating new job in Redis: {} for file: {}", jobId, fileName);
        redisTemplate.opsForValue().set(KEY_PREFIX + jobId, job);
        
        return jobId;
    }

    /**
     * Updates an existing job record in Redis.
     */
    public void updateJob(FlashcardJob job) {
        log.debug("Updating job status in Redis: {} to {}", job.getId(), job.getStatus());
        redisTemplate.opsForValue().set(KEY_PREFIX + job.getId(), job);
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
}
