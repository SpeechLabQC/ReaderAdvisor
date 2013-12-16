package readerAdvisor.speech.util;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * Test class for the StatisticalLanguageModelUtils class
 */
public class TestStatisticalLanguageModelUtils {

    @BeforeClass
    public static void setup() throws Exception{
        // Unable to Mock Singleton Class GlobalProperties
        //when(System.getProperty("configurationFile")).thenReturn("script/software.properties");
        System.setProperty("configurationFile", "script/software.properties");
    }

    @AfterClass
    public static void cleanup(){
        // Do nothing
    }

    @Test
    public void testCreateLanguageModel(){
        String text = "This is a test\nAnother line for the test\nresults";
        boolean lmCreated = StatisticalLanguageModelUtils.createLanguageModel("temp", "NGramModel", text);
        // Assert if the language model was not created
        assert(lmCreated);
    }
}
