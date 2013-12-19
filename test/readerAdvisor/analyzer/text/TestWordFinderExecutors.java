package readerAdvisor.analyzer.text;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for the Word Finder Executors
 */
public class TestWordFinderExecutors {

    @BeforeClass
    public static void setup() throws Exception{
        // Unable to Mock Singleton Class GlobalProperties
        //when(System.getProperty("configurationFile")).thenReturn("script/software.properties");
    }

    @AfterClass
    public static void cleanup(){
        // Do nothing
    }

    @Test
    public void testWordFinderCallableCounterExecutor(){
        // TODO: Implement it
    }

    @Test
    public void testWordFinderCallableExecutor(){
        // TODO: Implement it
    }

    @Test
    public void testWordFinderQueueExecutor(){
        // TODO: Implement it
    }
}
