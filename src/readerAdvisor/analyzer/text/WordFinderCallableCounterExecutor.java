package readerAdvisor.analyzer.text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Find the position of multiple words in the given line
 * returning the position of the encountered words once the thread stops running.
 *
 * Find the word in the given line and add it to the list of found words
 * It will look for the position of the words and once the thread has finished running, it will return a value.
 * The user can retrieve the n-th encountered of the word by setting the value of retrieveNthEncounter.
 */
public class WordFinderCallableCounterExecutor extends WordFinderExecutor {

    public WordFinderCallableCounterExecutor(List<String> words, String text){
        super(words,text);
    }

    public List<Position> call() throws InterruptedException, ExecutionException {
        // Keep track of the time that we start executing
        timeOfExecution = System.currentTimeMillis();
        List<Position> positions = new ArrayList<Position>();
        // Create the executor service
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Position position = new Position(0,text.length());
        // Create an executor for every words to look for
        for(String word : words){
            Callable<Position> finder = new WordFinderCallable(position,word,text);
            Future<Position> executor = executorService.submit(finder);
            // Wait until the word search has completed
            position = executor.get();
        }
        executorService.shutdown();
        // Update the execution time
        timeOfExecution = (System.currentTimeMillis()-timeOfExecution);
        return positions;
    }
}
