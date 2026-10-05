package com.flashcard.service;

import com.flashcard.parser.DocumentParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParserServiceTest {

    @Mock
    private DocumentParser pdfParser;

    @Mock
    private DocumentParser wordParser;

    @Test
    @DisplayName("parseDocument should throw IllegalArgumentException when filename is null")
    void parseDocument_NullFilename_ThrowsException() {
        ParserService parserService = new ParserService(List.of(pdfParser));

        assertThatThrownBy(() -> parserService.parseDocument(null, new byte[]{1, 2, 3}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Filename must contain an extension");
    }

    @Test
    @DisplayName("parseDocument should throw IllegalArgumentException when filename has no extension dot")
    void parseDocument_NoDotInFilename_ThrowsException() {
        ParserService parserService = new ParserService(List.of(pdfParser));

        assertThatThrownBy(() -> parserService.parseDocument("myNotesFile", new byte[]{1, 2, 3}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Filename must contain an extension");
    }

    @Test
    @DisplayName("parseDocument should route to matching parser and return extracted text")
    void parseDocument_MatchingParser_ReturnsText() throws Exception {
        byte[] content = "content bytes".getBytes();
        when(pdfParser.supports("pdf")).thenReturn(true);
        when(pdfParser.parse(content)).thenReturn("Extracted PDF content");

        ParserService parserService = new ParserService(List.of(wordParser, pdfParser));

        String result = parserService.parseDocument("lecture_notes.pdf", content);

        assertThat(result).isEqualTo("Extracted PDF content");
        verify(pdfParser, times(1)).supports("pdf");
        verify(pdfParser, times(1)).parse(content);
        verify(wordParser, times(1)).supports("pdf");
        verify(wordParser, never()).parse(any());
    }

    @Test
    @DisplayName("parseDocument should throw IllegalArgumentException when no parser supports the extension")
    void parseDocument_UnsupportedExtension_ThrowsException() {
        when(pdfParser.supports("xyz")).thenReturn(false);
        when(wordParser.supports("xyz")).thenReturn(false);

        ParserService parserService = new ParserService(List.of(pdfParser, wordParser));

        assertThatThrownBy(() -> parserService.parseDocument("archive.xyz", new byte[]{1, 2}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unsupported file format: xyz");

        verify(pdfParser, times(1)).supports("xyz");
        verify(wordParser, times(1)).supports("xyz");
    }
}
