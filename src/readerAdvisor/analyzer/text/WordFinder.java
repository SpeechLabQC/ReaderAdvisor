package readerAdvisor.analyzer.text;

import readerAdvisor.file.FileUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * Find the word in the given line and add it to the list of found words
 * It could be used with the ThreadPool in order to look for multiple words at once
 */
// TODO: Create jUnit test for all new classes
public class WordFinder extends Thread {

    protected final Position position;
    protected String word;
    protected String line;
    protected final AtomicReferenceArray<Position> positions;

    public WordFinder(Position position, String word, String line, AtomicReferenceArray<Position> positions){
        this.position = position;
        this.word = word;
        this.line = line;
        this.positions = positions;
    }

    // Return the start position for the next time that we'll read.
    // The start position must be updated when a new word is recognized.
    public Position getPosition(){
        return position;
    }

    public String getWord(){
        return word;
    }

    public String getLine(){
        return line;
    }

    public AtomicReferenceArray<Position> getPositions(){
        return positions;
    }

    public void run(){
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
                int indexInList = words.indexOf(word);
                // If the word is the first token, then find the position of the word itself - otherwise, add a space in front of the word.
                // Add space to avoid indexing the position of a word that contains another one. Ex.- 'Leather' contains 'the', 'Start' contains 'art'.
                position.increaseStartPositionBy(indexInList == 0 ? line.indexOf(word) : (line.indexOf(" "+word) + 1)); //Advance the space that was included
                // Update the end position
                position.setEndPosition(position.getStartPosition() + word.length());
                // Insert this updated position at the end of the list
                positions.set(positions.length(), position);
            }
        }
    }
}