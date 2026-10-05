package com.flashcard.parser;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PptParserTest {

    private final PptParser parser = new PptParser();

    @ParameterizedTest
    @ValueSource(strings = {"pptx", "PPTX", ".pptx", ".PPTX", "ppt", "PPT", ".ppt", ".PPT"})
    @DisplayName("supports should return true for valid PowerPoint extensions")
    void supports_ValidExtensions(String extension) {
        assertThat(parser.supports(extension)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"pdf", "docx", "txt", "xlsx", ""})
    @DisplayName("supports should return false for non-PowerPoint extensions")
    void supports_InvalidExtensions(String extension) {
        assertThat(parser.supports(extension)).isFalse();
    }

    @Test
    @DisplayName("supports should return false when extension is null")
    void supports_NullExtension() {
        assertThat(parser.supports(null)).isFalse();
    }

    @Test
    @DisplayName("parse should extract text from a valid in-memory PPTX presentation")
    void parse_ValidPptx() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            XSLFSlide slide = ppt.createSlide();
            XSLFTextBox textBox = slide.createTextBox();
            XSLFTextParagraph paragraph = textBox.addNewTextParagraph();
            XSLFTextRun run = paragraph.addNewTextRun();
            run.setText("Microservices Architecture Overview");
            ppt.write(baos);
        }

        String extracted = parser.parse(baos.toByteArray());

        assertThat(extracted).contains("Microservices Architecture Overview");
    }

    @Test
    @DisplayName("parse should throw Exception when invalid bytes are passed")
    void parse_InvalidBytes_ThrowsException() {
        byte[] corrupted = new byte[]{1, 3, 5, 7, 9};

        assertThatThrownBy(() -> parser.parse(corrupted))
                .isInstanceOf(Exception.class);
    }
}
