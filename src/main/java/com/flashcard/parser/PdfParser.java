package com.flashcard.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

/**
 * Implementation of DocumentParser for PDF files.
 * 
 * @Slf4j: A Lombok annotation that automatically creates a Logger field named 'log'.
 * This allows us to record what's happening in the application without manually 
 * creating a Logger instance.
 */
@Slf4j
@Component
public class PdfParser implements DocumentParser {

    @Override
    public boolean supports(String fileExtension) {
        return fileExtension != null &&
                (fileExtension.equalsIgnoreCase("pdf") || fileExtension.equalsIgnoreCase(".pdf"));
    }

    /**
     * Extracts text content from a PDF file.
     * 
     * @param fileContent The raw bytes of the PDF file.
     * @return The extracted text as a String.
     * @throws Exception If an error occurs during PDF processing.
     */
    @Override
    public String parse(byte[] fileContent) throws Exception {
        log.info("Starting PDF text extraction...");
        
        // try-with-resources: Automatically closes the PDDocument to prevent memory leaks.
        try (PDDocument document = Loader.loadPDF(fileContent)) {
            PDFTextStripper stripper = new PDFTextStripper();
            
            // Extract the text
            String text = stripper.getText(document);
            
            log.info("Successfully extracted {} characters from PDF.", text.length());
            return text;
        } catch (Exception e) {
            log.error("Failed to parse PDF file", e);
            throw e;
        }
    }
}
