package com.flashcard.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Implementation of DocumentParser for Word files (.docx).
 */
@Slf4j
@Component
public class WordParser implements DocumentParser {

    @Override
    public String parse(byte[] fileContent) throws Exception {
        log.info("Starting Word text extraction...");
        
        try (InputStream is = new ByteArrayInputStream(fileContent);
             XWPFDocument document = new XWPFDocument(is);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            
            String text = extractor.getText();
            log.info("Successfully extracted {} characters from Word document.", text.length());
            return text;
        } catch (Exception e) {
            log.error("Failed to parse Word file", e);
            throw e;
        }
    }

    @Override
    public boolean supports(String fileExtension) {
        return fileExtension != null && (
                fileExtension.equalsIgnoreCase("docx") || 
                fileExtension.equalsIgnoreCase(".docx") ||
                fileExtension.equalsIgnoreCase("doc") || 
                fileExtension.equalsIgnoreCase(".doc")
        );
    }
}
