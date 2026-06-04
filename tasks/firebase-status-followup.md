# Firebase Save Status Follow-Up

## Issue

Job status currently becomes `COMPLETED` even if saving the generated flashcards to Firebase fails.

Current flow:

1. Gemini succeeds.
2. Firebase save fails.
3. `FirebaseService` logs the error.
4. `FlashcardService` still marks the job as `COMPLETED`.

## Desired Behavior

If Firebase save fails, the job should become `FAILED`, and the job error should include the Firebase failure message.

Desired flow:

1. Gemini succeeds.
2. Firebase save fails.
3. Exception propagates back to `FlashcardService`.
4. `FlashcardService` marks the job as `FAILED`.

## Likely Fix

Update `FirebaseService.saveDeck()` to rethrow the exception after logging it.

Then let `FlashcardService.processFlashcards()` catch the exception and update the Redis job status to `FAILED`.

## Related Files

- `src/main/java/com/flashcard/service/FirebaseService.java`
- `src/main/java/com/flashcard/service/FlashcardService.java`
