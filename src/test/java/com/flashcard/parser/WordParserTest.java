package com.flashcard.parser;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordParserTest {

    private final WordParser parser = new WordParser();

    @ParameterizedTest
    @ValueSource(strings = {"docx", "DOCX", ".docx", ".DOCX", "doc", "DOC", ".doc", ".DOC"})
    @DisplayName("supports should return true for valid Word extensions")
    void supports_ValidExtensions(String extension) {
        assertThat(parser.supports(extension)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"pdf", "pptx", "txt", "xlsx", ""})
    @DisplayName("supports should return false for non-Word extensions")
    void supports_InvalidExtensions(String extension) {
        assertThat(parser.supports(extension)).isFalse();
    }

    @Test
    @DisplayName("supports should return false when extension is null")
    void supports_NullExtension() {
        assertThat(parser.supports(null)).isFalse();
    }

    @Test
    @DisplayName("parse should extract text from a valid in-memory DOCX document")
    void parse_ValidDocx() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("Introduction to Algorithms and Data Structures");
            document.write(baos);
        }

        String extracted = parser.parse(baos.toByteArray());

        assertThat(extracted).contains("Introduction to Algorithms and Data Structures");
    }

    @Test
    @DisplayName("parse should throw Exception when invalid bytes are passed")
    void parse_InvalidBytes_ThrowsException() {
        byte[] corrupted = new byte[]{10, 20, 30, 40};

        assertThatThrownBy(() -> parser.parse(corrupted))
                .isInstanceOf(Exception.class);
    }
}
