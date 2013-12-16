package readerAdvisor.speech.util;

import readerAdvisor.environment.EnvironmentUtils;
import readerAdvisor.environment.GlobalProperties;
import readerAdvisor.file.FileUtils;

import java.io.*;

/*
* Compiles and creates Statistical Language Models
*/
// TODO: Fix so that the folder gets deleted for JSGF Grammar
// TODO: Make sure to highlight complete words and not partial words.
// TODO: Fix the scren that it froze and displayed black. The screen should refresh every now and then automatically.
// TODO: Fix the text. why are there squares for? - Square represent quotes. Eliminate quotes from the text. Run perl scripts to clean up the text before creatin the grammar.
// TODO: Add a listener when typing a name at SphinxPropertiesWindow.getSaveConfigurationsPanel()
// TODO: Run perl scripts that will clean the text further
// TODO: Document that symbolic links must be created
// TODO: Highlight thread turn it to SwingUtils.InvokeLater to increase performance
// TODO: Highlight of words has a bug - FIX IT
// TODO: In order to understand what line is followed, then create a data structure that will work like a list
// TODO: [line]-[line]-[line] that will iterate through 2 before - same - 2 after (configurable or all list) to see which one is more accurate
// TODO: If there's no one accurate then do not highlight anything. Give a threshold for find the precision of which one is the line.
// TODO: This can be done in a thread that doesn't have to hold highlighting and speech recognition. This must be in the analyzer package.
public class StatisticalLanguageModelUtils {
    // Directory where the executable files are installed
    private static final String win32_directory  = GlobalProperties.getInstance().getProperty("statisticalLanguageModelUtils.win32");
    private static final String batch_script     = GlobalProperties.getInstance().getProperty("statisticalLanguageModelUtils.batch");
    private static final String unix_directory   = GlobalProperties.getInstance().getProperty("statisticalLanguageModelUtils.unix");
    private static final String shell_script     = GlobalProperties.getInstance().getProperty("statisticalLanguageModelUtils.shell");
    private static final boolean DELETE_ON_CLOSE = GlobalProperties.getInstance().getPropertyAsBoolean("statisticalLanguageModelUtils.deleteFilesOnClose");

    /**
     * Create a Language Model depending on the OS
     * @param dir Directory where the language model will reside
     * @param grammar Name of the grammar to store the .lm and .DMP files
     * @param text Text to be converted into the language model
     * @return True if the language model was created successfully. False otherwise
     */
    public static boolean createLanguageModel(String dir, String grammar, String text){
        return (EnvironmentUtils.isWindows() ?
                createLanguageModel(dir,grammar,text,win32_directory,batch_script)
                :
                createLanguageModel(dir,grammar,text,unix_directory,shell_script));
    }

    /**
     * Create the text file for the Statistical Language files to be created
     * @param grammar Name of the file
     * @param text Body of the file to be written
     * @param deleteOnExit True if the file must be delete when the program terminates
     * @throws IOException Throws IOException if the file was not created
     */
    private static void createTextFile(String grammar, String text, boolean deleteOnExit) throws IOException {
        File file = new File(grammar + ".txt");
        // Delete this file when exiting the JVM
        if(deleteOnExit){ file.deleteOnExit(); }
        // Write the Grammar into the document
        BufferedWriter bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
        for(String line : text.split("\n")){
            bw.write("<s> " + line.trim() + " </s>" + EnvironmentUtils.NEW_LINE);
        }
        bw.close();
    }

    /**
     * Get the name of the Grammar that will be run with respect to the executable files
     * @param bin Directory where the executable files reside
     * @param dir Directory where the Statistical Language Model files will be stored
     * @param grammar Name of the Grammar file
     * @return The Command to be execute. Return empty if either of the parameters used is null.
     */
    private static String getGrammarLanguageModelPath(String bin, String dir, String grammar){
        String path = "";
        if(bin != null && dir != null && grammar != null){
            // Run the batch script to create the Language Model
            StringBuilder cmd = new StringBuilder();
            // Split by '/' and not by File.separator because the file paths are set in software.properties file and they are written with '/'
            // They should remain like this because regex on '\' is difficult to perform
            int path_levels = bin.split("/").length;
            for(int level = 0; level < path_levels; level++){
                cmd.append("..").append(File.separator);
            }
            cmd.append(dir).append(File.separator).append(grammar);
            path = cmd.toString();
        }
        // Return the command
        return path;
    }

    /**
     * Detects whether the Statistical Language Model files were created
     * @param grammar Name of the Grammar file
     * @param deleteOnExit True if the file must be delete when the program terminates
     * @return True if the files exists
     */
    private static boolean isLanguageModelCreated(String grammar, boolean deleteOnExit){
        boolean isLanguageModelCreated = false;
        // Ensure that the files were created successfully
        File lm = new File(grammar + ".lm");
        File dmp = new File(grammar + ".DMP");
        // Check if the files exists
        if(lm.exists() && lm.isFile() && dmp.exists() && dmp.isFile()){
            // Delete files when the program exists
            if(deleteOnExit){
                lm.deleteOnExit();
                dmp.deleteOnExit();
            }
            // The Language Model was created successfully
            isLanguageModelCreated = true;
        }
        return isLanguageModelCreated;
    }

    /**
     * Write to the console the input stream and error stream that belong to the process that executes the script file
     * @param in Input Stream of the process
     * @param err Error Stream of the process
     */
    private static void writeStreamsToConsole(final BufferedReader in, final BufferedReader err){
        // Writing streams to the console can be done a Thread since their actions do not modify the return boolean
        if(in != null && err != null){
            new Thread(){
                public void run(){
                    try{// Write output to the consoles
                        String line;
                        // Input Stream console
                        while((line = in.readLine()) != null){ System.out.println(line); }
                        // Error Stream Console
                        while((line = err.readLine()) != null){ System.err.println(line); }
                    }catch (IOException e){ e.printStackTrace(); }
                }
            }.start();
        }
    }

    /**
     * Create a Language Model given the directory and sript of the specific Systems
     * @param dir Directory where the language model will reside
     * @param grammar Name of the grammar to store the .lm and .DMP files
     * @param text Text to be converted into the language model
     * @param bin Directory where the executable files reside
     * @param script Script file that will create the Statistical Language Model
     * @return True if the language model was created successfully. False otherwise
     */
    public static boolean createLanguageModel(String dir, String grammar, String text, String bin, String script){
        boolean isStatisticalLanguageModelCreatedSuccessfully = false;
        // Only proceed if the data is valid
        if(dir != null && grammar != null && text != null && bin != null && script != null){
            try{
                String temp_grammar = dir + File.separator + grammar;
                // Ensure tha the directory exists otherwise create it and ensure its deletion when the program terminates
                FileUtils.ensureDirectoryExistsWithReadWriteAccess(dir,DELETE_ON_CLOSE);
                // Only leave text and numbers
                text = FileUtils.normalizeNewLine(text);
                text = FileUtils.getTextAndDigits(text);
                // Write text to file
                createTextFile(temp_grammar, text, DELETE_ON_CLOSE);
                // Execute the process
                String _normalizeBin = new File(bin).getCanonicalPath();
                String _script = _normalizeBin + File.separator + script,
                       _grammar = dir + File.separator + grammar;
                Process process = new ProcessBuilder(_script,_grammar,_normalizeBin).start();
                BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                // Wait until the program has finish executing
                process.waitFor();
                int returnValue = process.exitValue();
                // Not negative integers are consider acceptable return values
                if(returnValue > -1) {
                    // Ensure that the files were created successfully
                    isStatisticalLanguageModelCreatedSuccessfully = isLanguageModelCreated(temp_grammar,DELETE_ON_CLOSE);
                }
                // Write the Streams to the console
                writeStreamsToConsole(in,err);
            } catch(Exception e){ e.printStackTrace(); }
        }
        return isStatisticalLanguageModelCreatedSuccessfully;
    }
}
