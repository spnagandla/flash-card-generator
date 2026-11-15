package com.flashcard.service;

import com.flashcard.model.FlashcardJob;
import com.flashcard.model.JobStatusResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Holds status requests until a job changes or the request reaches its timeout.
 */
@Service
public class JobStatusLongPollingService {

    private static final long LONG_POLL_TIMEOUT_MILLIS = 25_000L;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<DeferredResult<ResponseEntity<JobStatusResponse>>>> waiters =
            new ConcurrentHashMap<>();

    public DeferredResult<ResponseEntity<JobStatusResponse>> waitForUpdate(String jobId) {
        DeferredResult<ResponseEntity<JobStatusResponse>> result =
                new DeferredResult<>(LONG_POLL_TIMEOUT_MILLIS);

        waiters.computeIfAbsent(jobId, ignored -> new CopyOnWriteArrayList<>()).add(result);

        result.onTimeout(() -> result.setResult(new ResponseEntity<>(HttpStatus.NO_CONTENT)));
        result.onCompletion(() -> removeWaiter(jobId, result));

        return result;
    }

    public void publish(FlashcardJob job) {
        List<DeferredResult<ResponseEntity<JobStatusResponse>>> jobWaiters = waiters.remove(job.getId());
        if (jobWaiters == null) {
            return;
        }

        ResponseEntity<JobStatusResponse> response = ResponseEntity.ok(toResponse(job));
        jobWaiters.forEach(waiter -> waiter.setResult(response));
    }

    private void removeWaiter(
            String jobId,
            DeferredResult<ResponseEntity<JobStatusResponse>> result
    ) {
        waiters.computeIfPresent(jobId, (ignored, jobWaiters) -> {
            jobWaiters.remove(result);
            return jobWaiters.isEmpty() ? null : jobWaiters;
        });
    }

    public static JobStatusResponse toResponse(FlashcardJob job) {
        return new JobStatusResponse(
                job.getId(),
                job.getStatus(),
                job.getFileName(),
                job.getProgress(),
                job.getError()
        );
    }
}
