package com.flashcard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flashcard {
    private String question;
    private String answer;
    private String explanation;
    private List<String> distractors;
    private List<String> incorrectExplanations;
    private String difficulty;
    private List<String> tags;
    private String cognitiveLevel;
    private Double confidence;
    private String sourceSnippet;
}
