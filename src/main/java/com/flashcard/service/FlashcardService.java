package com.flashcard.service;

import com.flashcard.model.FlashcardJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for the asynchronous processing of flashcards.
 * It coordinates text extraction and (eventually) LLM generation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final ParserService parserService;
    private final JobService jobService;
    private final GeminiService geminiService;
    private final FirebaseService firebaseService;

    /**
     * The core background task. 
     * @Async tells Spring to run this in a separate thread.
     */
    @Async
    public void processFlashcards(String jobId, byte[] fileContent) {
        log.info("Starting background processing for job: {}", jobId);
        
        // 1. Retrieve the job from Redis
        FlashcardJob job = jobService.getJob(jobId);
        if (job == null) {
            log.error("Cannot process job: {} - not found in Redis", jobId);
            return;
        }

        try {
            // 2. Update status to PROCESSING
            job.setStatus("PROCESSING");
            jobService.updateJob(job);
            
            // 3. Extract text from the document
            log.info("Parsing document for job: {}", jobId);
            String text = parserService.parseDocument(job.getFileName(), fileContent);
            job.setExtractedText(text);
            
            // 4. Update progress
            log.info("Extraction complete for job: {}. Length: {} chars.", jobId, text.length());


            log.info("Sending extracted text to Gemini for job: {}", jobId);
            String flashcardsJson = geminiService.generateFlashcards(text);
            job.setFlashcardsJson(flashcardsJson);

            // Save to Firebase permanently
            log.info("Saving generated cards to Firebase for job: {}", jobId);
            firebaseService.saveDeck(jobId, job.getFileName(), flashcardsJson);
            
            // 5. Mark as COMPLETED (for now)
            job.setStatus("COMPLETED");
            jobService.updateJob(job);
            
        } catch (Exception e) {
            log.error("Error processing job: " + jobId, e);
            
            // 6. Update status to FAILED so the user knows what happened
            job.setStatus("FAILED");
            job.setError(e.getMessage());
            jobService.updateJob(job);
        }
    }
}
