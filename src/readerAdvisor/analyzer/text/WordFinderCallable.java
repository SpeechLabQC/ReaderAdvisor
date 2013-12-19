package readerAdvisor.analyzer.text;

import readerAdvisor.file.FileUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Find the word in the given line and add it to the list of found words
 * It will look for the position of the words and once the thread has finished running, it will return a value.
 */
public class WordFinderCallable implements Callable<Position> {

    protected final Position position;
    protected String word;
    protected String line;

    public WordFinderCallable(Position position, String word, String line){
        this.position = position;
        this.word = word;
        this.line = line;
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
                // Get the location of the word
                int indexInList = words.indexOf(word);
                // If the word is the first token, then find the position of the word itself - otherwise, add a space in front of the word.
                // Add space to avoid indexing the position of a word that contains another one. Ex.- 'Leather' contains 'the', 'Start' contains 'art'.
                position.increaseStartPositionBy(indexInList == 0 ? line.indexOf(word+" ") :  // Check if the word is located at the beginning
                        (indexInList == words.size()-1 ?           // Check if the word is located at the end
                                line.indexOf(" "+word) :
                                line.indexOf(" "+word+" ")
                        )+1); //Advance the space that was included
                // Update the end position
                position.setEndPosition(position.getStartPosition() + word.length());
                // Do not create a reference to the position object, only copy its values
                encounteredWord = new Position(position.getStartPosition(),position.getEndPosition());
            }
        }
        // Return the word
        return encounteredWord;
    }
}