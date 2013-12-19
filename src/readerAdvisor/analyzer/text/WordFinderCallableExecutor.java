package readerAdvisor.analyzer.text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Find the position of multiple words in the given line
 * returning the position of the encountered words once the thread stops running.
 */
public class WordFinderCallableExecutor extends WordFinderExecutor {

    public WordFinderCallableExecutor(List<String> words, String text){
        super(words,text);
    }

    public List<Position> call() throws InterruptedException, ExecutionException{
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
