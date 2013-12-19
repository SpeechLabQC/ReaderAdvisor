package readerAdvisor.analyzer.text;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * Find the position of multiple words in the given line.
 */
public abstract class WordFinderExecutor implements Callable<List<Position>> {

    protected List<String> words;
    protected String text;
    // Debugging parameter
    protected long timeOfExecution;

    public WordFinderExecutor(List<String> words, String text){
        this.words = words;
        this.text = text;
        this.timeOfExecution = -1;
    }

    public abstract List<Position> call() throws InterruptedException, ExecutionException;

    // Return the time that this Executor took in order to find the matches
    public long getTimeOfExecution(){
        return timeOfExecution;
    }
}
