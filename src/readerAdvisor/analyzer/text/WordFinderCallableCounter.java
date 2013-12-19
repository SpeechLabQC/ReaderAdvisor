package readerAdvisor.analyzer.text;

import readerAdvisor.file.FileUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Find the word in the given line and add it to the list of found words
 * It will look for the position of the words and once the thread has finished running, it will return a value.
 * The user can retrieve the n-th encountered of the word by setting the value of retrieveNthEncounter.
 */
public class WordFinderCallableCounter extends WordFinderCallable {

    protected int retrieveNthEncounter;

    public WordFinderCallableCounter(Position position, String word, String line, int retrieveNthEncounter){
        super(position,word,line);
        this.retrieveNthEncounter = retrieveNthEncounter;
    }

    public Position call(){
        // Word that was encountered
        Position encounteredWord = null;
        // We'll trust that the words are valid - another class should ensure to not pass null pointers
        int lengthToRead = position.getLength();
        // Clean up the word to search
        word = word.trim().toLowerCase();
        // Clean up the text from non-alphabetical characters
        line = FileUtils.getAlphabeticalChars(line.toLowerCase());
        // Only proceed if there's workable data
        if(lengthToRead > 0 && !word.isEmpty() && !line.isEmpty()){
            // Split the words by space in store them in a list
            List<String> words = Arrays.asList(line.split("\\s+"));
            // If there's a match, then highlight it
            if(words.contains(word)){
                // Encounter flag
                int wordFound = 0;
                // Index in the list where this word was found
                int indexInList = -1;
                // Position of the iteration
                int i = 0;
                // Iterate through the list
                for(String word : words){
                    // Keep the length of this word - plus the extra space
                    indexInList += words.get(i).length()+1;
                    // Check if the word in the list is the word that we are looking for
                    if(words.get(i).equals(word)){
                        // Update the found counter and Check if this is the Nth encounter
                        if(++wordFound == retrieveNthEncounter){
                            // If so, then update the position where this word was found
                            position.increaseStartPositionBy(indexInList);
                            // Update the end position
                            position.setEndPosition(position.getStartPosition() + word.length());
                            // Do not create a reference to the position object, only copy its values
                            encounteredWord = new Position(position.getStartPosition(),position.getEndPosition());
                            // Stop iterating through the loop
                            break;
                        }
                    }
                    // Increase the counter
                    i++;
                }
            }
        }
        // Return the word
        return encounteredWord;
    }
}