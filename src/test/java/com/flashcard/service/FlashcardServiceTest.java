package com.flashcard.service;

import com.flashcard.model.FlashcardJob;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceTest {

    @Mock
    private ParserService parserService;

    @Mock
    private JobService jobService;

    @Mock
    private GeminiService geminiService;

    @Mock
    private FirebaseService firebaseService;

    @InjectMocks
    private FlashcardService flashcardService;

    @Test
    @DisplayName("processFlashcards should exit immediately if job is not found in Redis")
    void processFlashcards_JobNotFound_ExitsGracefully() {
        when(jobService.getJob("job-none")).thenReturn(null);

        flashcardService.processFlashcards("job-none", new byte[]{1, 2});

        verify(jobService, times(1)).getJob("job-none");
        verify(jobService, never()).updateJob(any());
        verifyNoInteractions(parserService, geminiService, firebaseService);
    }

    @Test
    @DisplayName("processFlashcards should execute full workflow and mark job COMPLETED")
    void processFlashcards_Success() throws Exception {
        byte[] content = "dummy file content".getBytes();
        FlashcardJob job = FlashcardJob.builder()
                .id("job-100")
                .fileName("test.pdf")
                .status("PENDING")
                .build();

        when(jobService.getJob("job-100")).thenReturn(job);
        when(parserService.parseDocument("test.pdf", content)).thenReturn("Extracted document text");
        when(geminiService.generateFlashcards("Extracted document text")).thenReturn("[{\"question\":\"Q?\"}]");

        flashcardService.processFlashcards("job-100", content);

        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        assertThat(job.getExtractedText()).isEqualTo("Extracted document text");
        assertThat(job.getFlashcardsJson()).isEqualTo("[{\"question\":\"Q?\"}]");
        assertThat(job.getError()).isNull();

        verify(parserService, times(1)).parseDocument("test.pdf", content);
        verify(geminiService, times(1)).generateFlashcards("Extracted document text");
        verify(firebaseService, times(1)).saveDeck("job-100", "test.pdf", "[{\"question\":\"Q?\"}]");
        // updateJob is called twice: once for PROCESSING, once for COMPLETED
        verify(jobService, times(2)).updateJob(job);
    }

    @Test
    @DisplayName("processFlashcards should mark job as FAILED when document parsing throws exception")
    void processFlashcards_ParserFailure_SetsJobFailed() throws Exception {
        byte[] content = "corrupt content".getBytes();
        FlashcardJob job = FlashcardJob.builder()
                .id("job-101")
                .fileName("corrupt.pdf")
                .status("PENDING")
                .build();

        when(jobService.getJob("job-101")).thenReturn(job);
        when(parserService.parseDocument("corrupt.pdf", content))
                .thenThrow(new IllegalArgumentException("Unsupported file format: pdf"));

        flashcardService.processFlashcards("job-101", content);

        assertThat(job.getStatus()).isEqualTo("FAILED");
        assertThat(job.getError()).isEqualTo("Unsupported file format: pdf");
        verify(geminiService, never()).generateFlashcards(any());
        verify(firebaseService, never()).saveDeck(any(), any(), any());
        verify(jobService, times(2)).updateJob(job);
    }

    @Test
    @DisplayName("processFlashcards should mark job as FAILED when GeminiService throws exception")
    void processFlashcards_GeminiFailure_SetsJobFailed() throws Exception {
        byte[] content = "sample content".getBytes();
        FlashcardJob job = FlashcardJob.builder()
                .id("job-102")
                .fileName("sample.pdf")
                .status("PENDING")
                .build();

        when(jobService.getJob("job-102")).thenReturn(job);
        when(parserService.parseDocument("sample.pdf", content)).thenReturn("Parsed text");
        when(geminiService.generateFlashcards("Parsed text"))
                .thenThrow(new IllegalStateException("Gemini quota exceeded"));

        flashcardService.processFlashcards("job-102", content);

        assertThat(job.getStatus()).isEqualTo("FAILED");
        assertThat(job.getError()).isEqualTo("Gemini quota exceeded");
        verify(firebaseService, never()).saveDeck(any(), any(), any());
        verify(jobService, times(2)).updateJob(job);
    }
}
