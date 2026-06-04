package com.flashcard.parser;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Implementation of DocumentParser for PowerPoint files (.pptx).
 * 
 * Uses SlideShowExtractor which is the modern way to extract text from 
 * various SlideShow formats in Apache POI.
 */
@Slf4j
@Component
public class PptParser implements DocumentParser {

    @Override
    public String parse(byte[] fileContent) throws Exception {
        log.info("Starting PowerPoint text extraction...");
        
        try (InputStream is = new ByteArrayInputStream(fileContent);
             XMLSlideShow ppt = new XMLSlideShow(is);
             SlideShowExtractor<org.apache.poi.xslf.usermodel.XSLFShape, org.apache.poi.xslf.usermodel.XSLFTextParagraph> extractor = new SlideShowExtractor<>(ppt)) {

            // We configure the extractor to include text from all parts of the slide
            extractor.setMasterByDefault(true);
            extractor.setNotesByDefault(true);
            
            String text = extractor.getText();
            log.info("Successfully extracted {} characters from PowerPoint document.", text.length());
            return text;

        } catch (Exception e) {
            log.error("Failed to parse PowerPoint file", e);
            throw e;
        }
    }

    @Override
    public boolean supports(String fileExtension) {
        return fileExtension != null && (
                fileExtension.equalsIgnoreCase("pptx") || 
                fileExtension.equalsIgnoreCase(".pptx") ||
                fileExtension.equalsIgnoreCase("ppt") || 
                fileExtension.equalsIgnoreCase(".ppt")
        );
    }
}
