package com.flashcard.service;

import com.flashcard.parser.DocumentParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Orchestrator service that selects the appropriate DocumentParser 
 * based on the file extension and handles the text extraction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ParserService {

    private final List<DocumentParser> parsers;


    public String parseDocument(String fileName, byte[] content) throws Exception {
        log.info("Received request to parse file: {}", fileName);

        // 1. Extract extension (e.g., "my_notes.pdf" -> "pdf")
        if (fileName == null || !fileName.contains(".")) {
            log.error("Invalid filename provided: {}", fileName);
            throw new IllegalArgumentException("Filename must contain an extension");
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        log.debug("Extracted extension: {}", extension);

        // 2. Find the right parser
        return parsers.stream()
                .filter(parser -> {
                    boolean supported = parser.supports(extension);
                    if (supported) {
                        log.info("Found matching parser: {}", parser.getClass().getSimpleName());
                    }
                    return supported;
                })
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No parser available for extension: {}", extension);
                    return new IllegalArgumentException("Unsupported file format: " + extension);
                })
                .parse(content);
    }
}
