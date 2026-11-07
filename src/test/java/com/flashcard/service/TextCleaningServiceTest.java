package com.flashcard.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextCleaningServiceTest {

    private final TextCleaningService cleaningService = new TextCleaningService();

    @Test
    void nullOrBlankTextReturnsEmptyString() {
        assertThat(cleaningService.clean(null)).isEmpty();
        assertThat(cleaningService.clean("   \n ")).isEmpty();
    }

    @Test
    void contentIsKeptUnchanged() {
        String text = "Dependency injection lets Spring provide dependencies.\n\nIt reduces coupling.";

        assertThat(cleaningService.clean(text)).isEqualTo(text);
    }

    @Test
    void pageNumbersAndCopyrightLinesAreRemoved() {
        String text = """
                Spring basics
                © 2026 Acme Corp. All rights reserved.
                Page 3 of 20
                7
                Beans are managed by the container.
                CONFIDENTIAL
                """;

        assertThat(cleaningService.clean(text))
                .isEqualTo("Spring basics\nBeans are managed by the container.");
    }

    @Test
    void repeatedContentLinesAreKept() {
        String text = "Example\nFirst case\n\nExample\nSecond case\n\nExample\nThird\n\nExample\nFourth";

        assertThat(cleaningService.clean(text)).isEqualTo(text);
    }

    @Test
    void sentenceMentioningCopyrightIsKept() {
        String text = "Software is protected by copyright law.";

        assertThat(cleaningService.clean(text)).isEqualTo(text);
    }

    @Test
    void paragraphBreaksArePreservedForChunking() {
        String text = "First paragraph.\n\n\n\nSecond paragraph.";

        assertThat(cleaningService.clean(text)).isEqualTo("First paragraph.\n\nSecond paragraph.");
    }
}
