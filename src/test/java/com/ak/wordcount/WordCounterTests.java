package com.ak.wordcount;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ak.wordcount.interfaces.Translator;

@ExtendWith(MockitoExtension.class)
public class WordCounterTests {
	
	private WordCounter wordCounter;

	 @Mock
	 private Translator translator;
	 
	 @BeforeEach
	    public void setUp() {
	        wordCounter = new WordCounter(translator);
	    }
	
	@Test
    public void testAddValidWord() {
		when(translator.translate("flower")).thenReturn("flower");
		
        wordCounter.addWord("flower");
        assertEquals(1, wordCounter.getWordCount("flower"));
    }
	
	@Test
	public void testAddMultipleWords() {
		when(translator.translate("flower")).thenReturn("flower");
		when(translator.translate("sunflower")).thenReturn("sunflower");
		
		wordCounter.addWord("flower sunflower");
        assertEquals(1, wordCounter.getWordCount("flower"));
        assertEquals(1, wordCounter.getWordCount("sunflower"));
	}
	
	@Test
    public void testAddWordWithNonAlphabeticCharacters() {
		when(translator.translate("flower")).thenReturn("flower");
		when(translator.translate("sunflower")).thenReturn("sunflower");
		// when(translator.translate("flower123")).thenReturn("flower123");   -->No need to actually mock it since its going to be ignored anyway
		
        wordCounter.addWord("flower flower123 sunflower");
        assertEquals(1, wordCounter.getWordCount("sunflower"));
        assertEquals(1, wordCounter.getWordCount("flower"));
        // Since "flower123" is invalid, it should not be added, and its count should be 0
        assertEquals(0, wordCounter.getWordCount("flower123"));
    }
	
	@Test
    public void testTranslationAndAddition() {
        when(translator.translate("flor")).thenReturn("flower");
        when(translator.translate("blume")).thenReturn("flower");
        when(translator.translate("flower")).thenReturn("flower");

        wordCounter.addWord("flor blume");
        assertEquals(2, wordCounter.getWordCount("flower"));
    }

    @Test
    public void testGetCountOfNonExistingWord() {
    	when(translator.translate("rose")).thenReturn("rose");
    	
        assertEquals(0, wordCounter.getWordCount("rose"));
    }
    
    @Test
    public void testConcurrentWordAddition() {
    	when(translator.translate("flower")).thenReturn("flower");
    	
        String text = IntStream.range(0, 10000)
                               .mapToObj(i -> "flower")
                               .collect(Collectors.joining(" "));

        // Simulating concurrent access by adding the same text in multiple threads
        IntStream.range(0, 10).parallel().forEach(i -> wordCounter.addWord(text));

        // Each "flower" in the text is added 10,000 times in 10 different threads
        assertEquals(100000, wordCounter.getWordCount("flower"));
    }
    
    @Test
    public void testLargeDataProcessing() {
    	when(translator.translate("flower")).thenReturn("flower");
        when(translator.translate("tree")).thenReturn("tree");
        when(translator.translate("sunflower")).thenReturn("sunflower");
        when(translator.translate("daisy")).thenReturn("daisy");
        when(translator.translate("rose")).thenReturn("rose");
        when(translator.translate("flor")).thenReturn("flower");
        when(translator.translate("orchid")).thenReturn("orchid");
        when(translator.translate("blume")).thenReturn("flower");
    	
    	
        String largeText = TestDataGenerator.generateLargeTextSet(10000); // Generate a large text with 100,000 words
        wordCounter.addWord(largeText);

        int expectedFlowerCount = countFlowerOccurrences(largeText, "flower");
        assertEquals(expectedFlowerCount, wordCounter.getWordCount("flower"));
        
        int expectedWordCount = countOccurrences(largeText, "tree");
        assertEquals(expectedWordCount, wordCounter.getWordCount("tree"));
    }
    
    @Test
    public void testLargeDataWithInvalidWordsProcessing() {
    	when(translator.translate("flower")).thenReturn("flower");
        when(translator.translate("tree")).thenReturn("tree");
        when(translator.translate("sunflower")).thenReturn("sunflower");
        when(translator.translate("rose")).thenReturn("rose");
        // when(translator.translate("da33isy")).thenReturn("da33isy");		---> Not needed since they wont be sent to the translator
        // when(translator.translate("4ora")).thenReturn("4ora");
    	
    	
        String largeText = TestDataGenerator.generateLargeTextSetWithInvalidWords(100000); // Generate a large text with 10,000 words
        wordCounter.addWord(largeText);
        
        int expectedTreeCount = countOccurrences(largeText, "tree");
        assertEquals(expectedTreeCount, wordCounter.getWordCount("tree"));
        
        int actualInvalidWordCount = countOccurrences(largeText, "da33isy");
        assertNotEquals(actualInvalidWordCount, wordCounter.getWordCount("da33isy"));
        assertEquals(0, wordCounter.getWordCount("da33isy"));
    }
    
    // Adding two separate instances for brevity. In actual implementation, 
    // since translate would function, so we can combine both
    private int countFlowerOccurrences(String text, String word) {
        return (int) Arrays.stream(text.split("\\W+"))
                           .filter(w -> w.equals(word) || w.equals("flor") || w.equals("blume"))
                           .count();
    }
    
    private int countOccurrences(String text, String word) {
        return (int) Arrays.stream(text.split("\\W+"))
                           .filter(word::equals)
                           .count();
    }
}
