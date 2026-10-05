package com.flashcard.parser;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PdfParserTest {

    private final PdfParser parser = new PdfParser();

    @ParameterizedTest
    @ValueSource(strings = {"pdf", "PDF", ".pdf", ".PDF"})
    @DisplayName("supports should return true for valid PDF extensions")
    void supports_ValidExtensions(String extension) {
        assertThat(parser.supports(extension)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"docx", "pptx", "txt", "png", ""})
    @DisplayName("supports should return false for non-PDF extensions")
    void supports_InvalidExtensions(String extension) {
        assertThat(parser.supports(extension)).isFalse();
    }

    @Test
    @DisplayName("supports should return false when extension is null")
    void supports_NullExtension() {
        assertThat(parser.supports(null)).isFalse();
    }

    @Test
    @DisplayName("parse should extract text from a valid in-memory PDF")
    void parse_ValidPdf() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                cs.newLineAtOffset(50, 700);
                cs.showText("Hello Flashcard Study Guide");
                cs.endText();
            }
            doc.save(baos);
        }

        String extracted = parser.parse(baos.toByteArray());

        assertThat(extracted).contains("Hello Flashcard Study Guide");
    }

    @Test
    @DisplayName("parse should throw Exception when invalid bytes are passed")
    void parse_InvalidBytes_ThrowsException() {
        byte[] corrupted = new byte[]{0, 1, 2, 3, 4, 5};

        assertThatThrownBy(() -> parser.parse(corrupted))
                .isInstanceOf(IOException.class);
    }
}
