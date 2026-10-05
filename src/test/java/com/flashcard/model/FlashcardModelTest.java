package com.flashcard.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlashcardModelTest {

    @Test
    @DisplayName("Flashcard getters and setters should correctly assign and return values")
    void testFlashcardGettersAndSetters() {
        Flashcard card = new Flashcard();
        card.setQuestion("What is a JVM?");
        card.setAnswer("Java Virtual Machine");
        card.setExplanation("It executes Java bytecode.");
        card.setDistractors(List.of("Java Variable Memory", "Just Valid Model", "Joint Vector Map"));
        card.setIncorrectExplanations(List.of("Not memory", "Not a model", "Not a vector map"));
        card.setDifficulty("easy");
        card.setTags(List.of("java", "jvm"));
        card.setCognitiveLevel("remember");
        card.setConfidence(0.99);
        card.setSourceSnippet("The JVM is responsible for bytecode execution.");

        assertThat(card.getQuestion()).isEqualTo("What is a JVM?");
        assertThat(card.getAnswer()).isEqualTo("Java Virtual Machine");
        assertThat(card.getExplanation()).isEqualTo("It executes Java bytecode.");
        assertThat(card.getDistractors()).hasSize(3).contains("Java Variable Memory");
        assertThat(card.getIncorrectExplanations()).hasSize(3);
        assertThat(card.getDifficulty()).isEqualTo("easy");
        assertThat(card.getTags()).containsExactly("java", "jvm");
        assertThat(card.getCognitiveLevel()).isEqualTo("remember");
        assertThat(card.getConfidence()).isEqualTo(0.99);
        assertThat(card.getSourceSnippet()).isEqualTo("The JVM is responsible for bytecode execution.");
    }

    @Test
    @DisplayName("Flashcard all-args constructor, equals, hashCode and toString should work properly")
    void testFlashcardAllArgsConstructorAndEquals() {
        Flashcard card1 = new Flashcard(
                "Q1", "A1", "E1",
                List.of("D1", "D2", "D3"),
                List.of("IE1", "IE2", "IE3"),
                "medium",
                List.of("tag1"),
                "understand",
                0.88,
                "Snippet"
        );

        Flashcard card2 = new Flashcard(
                "Q1", "A1", "E1",
                List.of("D1", "D2", "D3"),
                List.of("IE1", "IE2", "IE3"),
                "medium",
                List.of("tag1"),
                "understand",
                0.88,
                "Snippet"
        );

        assertThat(card1).isEqualTo(card2);
        assertThat(card1.hashCode()).isEqualTo(card2.hashCode());
        assertThat(card1.toString()).contains("Q1", "A1", "medium");
    }
}
