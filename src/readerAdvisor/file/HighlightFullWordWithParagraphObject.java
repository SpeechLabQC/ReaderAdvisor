package readerAdvisor.file;

import javax.swing.text.DefaultHighlighter;
import java.util.Arrays;
import java.util.List;

/**
 * Highlights the words - This class is replaced by readerAdvisor.analyzer.text.WordFinder.
 * This class uses Threads for a GUI. SwingUtilities should be used to handle GUI's actions.
 */
public class HighlightFullWordWithParagraphObject extends HighlightWord {
    // Return the start position for the next time that we'll read.
    // The start position must be updated when a new word is recognized.
    public int getPosition(){
        return startPosition;
    }

    // Highlight words with the exact length for each line
    public void run(){
        // If there is not word to recognized or the word is empty then do not recognize
        if(word == null || word.trim().isEmpty()){
            return;
        }
        // Get the highlighter
        DefaultHighlighter.DefaultHighlightPainter highLighter = FileUtils.getInstance().getHighlighter();
        // Get the word to highlight
        word = word.trim().toLowerCase();
        // Iterate through the document text
        int lengthToRead = endPosition - startPosition;
        if(lengthToRead > 0){
            try {
                String line = textPane.getDocument().getText(startPosition, lengthToRead).toLowerCase();
                // Replace the non-alphabetical characters for spaces
                line = FileUtils.getAlphabeticalChars(line);
                // Split the words by space in store them in a list
                List<String> words = Arrays.asList(line.split("\\s+"));
                // If there's a match, then highlight it
                if(words.contains(word)){
                    int indexInList = words.indexOf(word);
                    // If the word is the first token, then find the position of the word itself
                    // otherwise, add a space in front of the word.
                    // Avoid indexing the position of a word that contains another one. Ex.- 'Leather' contains 'the', 'Start' contains 'art'.
                    int offset = (indexInList == 0 ? line.indexOf(word+" ") :  // Check if the word is located at the beginning
                                    (indexInList == words.size()-1 ?           // Check if the word is located at the end
                                            line.indexOf(" "+word) :
                                            line.indexOf(" "+word+" ")
                                    )+1); //Advance the space that was included
                    // The new offset position is the actual position plus the offset (the position in the line)
                    offset += startPosition;
                    // Update the position to return it to the user
                    startPosition = offset + word.length();
                    // Highlight the word
                    textPane.getHighlighter().addHighlight(offset, startPosition, highLighter);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}