package readerAdvisor.analyzer.text;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Find the position of multiple words in the given line
 * using an AtomicArray to store the position of the encountered words.
 */
public class WordFinderQueueExecutor extends WordFinderExecutor {

    public WordFinderQueueExecutor(List<String> words, String text){
        super(words,text);
    }

    public List<Position> call() throws InterruptedException, ExecutionException {
        // Keep track of the time that we start executing
        timeOfExecution = System.currentTimeMillis();
        // LOGIC
        // Update the execution time
        timeOfExecution = (System.currentTimeMillis()-timeOfExecution);
        return null;
    }
}
