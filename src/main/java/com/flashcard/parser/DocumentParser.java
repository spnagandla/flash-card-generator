package com.flashcard.parser;

public interface DocumentParser {
    String parse(byte[] fileContent) throws Exception;
    boolean supports(String fileExtension);
}
