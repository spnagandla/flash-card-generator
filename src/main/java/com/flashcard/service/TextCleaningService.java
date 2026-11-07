package com.flashcard.service;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Removes obvious boilerplate (page numbers, copyright and confidentiality lines)
 * from parsed document text. Anything subtler, such as company-specific footers
 * or ads, is left to the Gemini prompt.
 */
@Service
public class TextCleaningService {

    private static final Pattern BOILERPLATE = Pattern.compile(
            "^(?:"
                    + "\\d{1,4}"
                    + "|(?:page|slide)\\s+\\d+(?:\\s*(?:of|/)\\s*\\d+)?"
                    + "|[-–—]\\s*\\d+\\s*[-–—]"
                    + "|(?:©|\\(c\\)|copyright\\b).*"
                    + "|all rights reserved.*"
                    + "|(?:strictly\\s+)?(?:confidential|proprietary)(?:\\s+(?:and|&)\\s+\\w+)?\\W*"
                    + ")$",
            Pattern.CASE_INSENSITIVE
    );

    public String clean(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        StringBuilder cleaned = new StringBuilder();
        boolean paragraphBreakPending = false;

        for (String line : text.split("\\R")) {
            String trimmed = line.strip();

            if (trimmed.isEmpty()) {
                paragraphBreakPending = true;
                continue;
            }
            if (BOILERPLATE.matcher(trimmed).matches()) {
                continue;
            }

            if (!cleaned.isEmpty()) {
                // Keep a paragraph break if the original had one, so the chunker can still split on it.
                cleaned.append(paragraphBreakPending ? "\n\n" : "\n");
            }
            cleaned.append(trimmed);
            paragraphBreakPending = false;
        }

        return cleaned.toString();
    }
}
