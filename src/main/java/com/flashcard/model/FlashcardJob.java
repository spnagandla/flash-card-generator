package com.flashcard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Builder is user to implement the builder pattern a way to build the objects
public class FlashcardJob implements Serializable {
    // A version ID for serialization (standard Java practice)
   private static final long serialVersionUID = 1L;

   private String id;
   private String status;
   private String fileName;
   private String flashcardsJson;
   private String extractedText;
   private String error;

}
