package com.flashcard.controller;

import com.flashcard.model.FlashcardDeck;
import com.flashcard.model.FlashcardResultResponse;
import com.flashcard.model.FlashcardJob;
import com.flashcard.model.JobStatusResponse;
import com.flashcard.model.UploadResponse;
import com.flashcard.service.DeckPersistenceService;
import com.flashcard.service.FlashcardService;
import com.flashcard.service.JobService;
import com.flashcard.service.JobStatusLongPollingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * REST Controller for flashcard operations.
 * Handles file uploads and status polling.
 */
@RestController
@Slf4j
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
public class FlashcardController {

    // These must be 'final' for @RequiredArgsConstructor to inject them!
    private final JobService jobService;
    private final FlashcardService flashcardService;
    private final DeckPersistenceService deckPersistenceService;
    private final JobStatusLongPollingService longPollingService;

    /**
     * Endpoint to upload a document and start the background processing.
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        log.info("Received upload request for file: {}", file.getOriginalFilename());
        
        // 1. Create a ticket (jobId)
        String jobId = jobService.createJob(file.getOriginalFilename());
        
        // 2. Start the work in the background
        flashcardService.processFlashcards(jobId, file.getBytes());
        
        // 3. Give the ticket back to the user immediately
        return ResponseEntity.ok(new UploadResponse(jobId));
    }

    /**
     * Endpoint to check the status of a specific job.
     * 
     * @param jobId The ID returned from the /upload endpoint.
     * @return The current state of the job (PENDING, PROCESSING, COMPLETED, or FAILED).
     */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<JobStatusResponse> getStatus(@PathVariable String jobId) {
        log.debug("Checking status for job: {}", jobId);
        
        FlashcardJob job = jobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(JobStatusLongPollingService.toResponse(job));
    }

    /**
     * Waits until the job changes from the status already known by the browser.
     * Returns 204 after the long-poll timeout so the browser can reconnect.
     */
    @GetMapping("/status/{jobId}/wait")
    public DeferredResult<ResponseEntity<JobStatusResponse>> waitForStatusChange(
            @PathVariable String jobId,
            @RequestParam(required = false) String knownStatus
    ) {
        FlashcardJob currentJob = jobService.getJob(jobId);
        if (currentJob == null) {
            return completedResult(ResponseEntity.notFound().build());
        }

        boolean statusAlreadyChanged = knownStatus == null ||
                !knownStatus.equals(currentJob.getStatus()) ||
                isFinished(currentJob.getStatus());

        if (statusAlreadyChanged) {
            return completedResult(ResponseEntity.ok(
                    JobStatusLongPollingService.toResponse(currentJob)));
        }

        DeferredResult<ResponseEntity<JobStatusResponse>> result =
                longPollingService.waitForUpdate(jobId);

        // Re-read after registration so an update between the first read and registration is not missed.
        FlashcardJob latestJob = jobService.getJob(jobId);
        if (latestJob != null && !knownStatus.equals(latestJob.getStatus())) {
            longPollingService.publish(latestJob);
        }

        return result;
    }

    @GetMapping("/result/{jobId}")
    public ResponseEntity<FlashcardResultResponse> getResult(@PathVariable String jobId) throws Exception {
        log.debug("Getting result for job: {}", jobId);

        FlashcardDeck deck = deckPersistenceService.findDeck(jobId).orElse(null);
        if (deck != null) {
            return ResponseEntity.ok(new FlashcardResultResponse(
                    deck.getJobId(),
                    deck.getFileName(),
                    deck.getFlashcards()
            ));
        }

        FlashcardJob job = jobService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        if (!"COMPLETED".equals(job.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        return ResponseEntity.notFound().build();
    }

    private boolean isFinished(String status) {
        return "COMPLETED".equals(status) || "FAILED".equals(status);
    }

    private DeferredResult<ResponseEntity<JobStatusResponse>> completedResult(
            ResponseEntity<JobStatusResponse> response
    ) {
        DeferredResult<ResponseEntity<JobStatusResponse>> result = new DeferredResult<>();
        result.setResult(response);
        return result;
    }
}
