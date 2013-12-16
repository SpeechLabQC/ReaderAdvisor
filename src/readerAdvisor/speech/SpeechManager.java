package readerAdvisor.speech;

import edu.cmu.sphinx.frontend.FrontEnd;
import edu.cmu.sphinx.frontend.util.StreamDataSource;
import edu.cmu.sphinx.util.props.PropertySheet;
import readerAdvisor.environment.GlobalProperties;
import readerAdvisor.file.FileUtils;
import readerAdvisor.file.HighlightItem;
import readerAdvisor.gui.DebuggerWindow;
import readerAdvisor.gui.RecognizerActionToolbar;
import readerAdvisor.gui.TextWindow;
import readerAdvisor.speech.util.AudioUtils;
import readerAdvisor.speech.util.GrammarUtils;
import readerAdvisor.speech.util.StatisticalLanguageModelUtils;

import javax.sound.sampled.AudioInputStream;

public class SpeechManager {
    /*
     * Speech Manager and grammar configuration files
     * - SpeechConfiguration : File where all the Speech Manager (Sphinx) information will be retrieve
     * - GRAMMAR : Name of the grammar to be used. It will be used when creating .gram files.
     * - GRAMMAR_FILE : Name of the grammar file.
     * - DIRECTORY : Directory name where the .gram files will be stored. This directory will be created and destroyed at runtime.
     */
    public String speechConfiguration;
    public static String GRAMMAR;                   //"JSGFGrammar";
    public static String GRAMMAR_FILE;              //"JSGFGrammar.gram";
    //public static String LANGUAGE_MODEL_FILE;       //"NGramModel.lm";
   // public static String BINARY_LANGUAGE_MODEL_FILE;//"NGramModel.DMP";
    // This directory since it is hardcoded in the *.config.xml files.
    public static final String DIRECTORY     = "temp";
    // Hard Coded Grammar model
    public static final String GRAMMAR_MODEL = "config/JSGFGrammar.config.xml";

    private static volatile SpeechManager speechManager = new SpeechManager();
    private static volatile LiveRecognizer liveRecognizer;

    /*
    * The items to be highlighter should not be chosen. Everything 'recognized', 'to be recognized' and 'errors'
    * should be highlighted. Only the option for colors/no-color should be given to the user.
    */
    // Highlight preferences
    private HighlightItem highlightItem = HighlightItem.HYPOTHESIS;

    // This language models should be defined in the software.properties file
    // speechManager.speechConfiguration.${LanguageModels} where ${LanguageModels} is a value from the bellow list.
    public static enum LanguageModels { JSGFGrammar, NGramModel }

    // The Language Model of this class
    private LanguageModels languageModel;

    private SpeechManager(){
        // Get the Language Model
        String languageModel = setAndVerifyLanguageModel(GlobalProperties.getInstance().getProperty("sphinx.languageModel"));
        // Provide a values if the property doesn't exists
        speechConfiguration = GlobalProperties.getInstance().getProperty("speechManager.speechConfiguration." + languageModel, GRAMMAR_MODEL);
        // Update the Grammar model
        updateLanguageModelFileNames();
    }

    public static SpeechManager getInstance(){
        return speechManager;
    }

    public Object clone() throws CloneNotSupportedException{
        throw new CloneNotSupportedException();
    }

    private void updateLanguageModelFileNames(){
        GRAMMAR = FileUtils.getTextWithoutPath(speechConfiguration);
        if(GRAMMAR.contains(".")){
            GRAMMAR = GRAMMAR.substring(0, GRAMMAR.indexOf("."));
        }
        GRAMMAR_FILE = GRAMMAR + ".gram";
        //LANGUAGE_MODEL_FILE = GRAMMAR + ".lm";
        //BINARY_LANGUAGE_MODEL_FILE = GRAMMAR + ".DMP";
    }

    /*
     * The items to be highlighter should not be chosen. Everything 'recognized', 'to be recognized' and 'errors'
     * should be highlighted. Only the option for colors/no-color should be given to the user.
     */
    @Deprecated
    public synchronized HighlightItem getHighlightItem(){
        return highlightItem;
    }

    /*
     * The items to be highlighter should not be chosen. Everything 'recognized', 'to be recognized' and 'errors'
     * should be highlighted. Only the option for colors/no-color should be given to the user.
     */
    @Deprecated
    public synchronized void setHighlightItem(HighlightItem highlightItem){
        this.highlightItem = highlightItem;
    }


    public synchronized void setSpeechConfiguration(String speechConfiguration){
        this.speechConfiguration = speechConfiguration;
    }

    public synchronized String getSpeechConfiguration(){
        return this.speechConfiguration;
    }

    private boolean isLanguageModelValid(String languageModel){
        boolean isLanguageModelValid = true;
        try{ LanguageModels.valueOf(languageModel); }
        catch(IllegalArgumentException e){ isLanguageModelValid = false; }
        return isLanguageModelValid;
    }

    public synchronized String setAndVerifyLanguageModel(String lm){
        // If the Language Model is not valid, then set a default one
        if(!isLanguageModelValid(lm)){
            languageModel = SpeechManager.LanguageModels.JSGFGrammar;
        }
        // If the Language Model is valid, then get its value
        else{
            languageModel = LanguageModels.valueOf(lm);
        }
        return languageModel.toString();
    }

    /*
     * Set up the Recognizer respective to their language model
     */
    public synchronized void configureRecognizer() {
        switch (languageModel){
            case JSGFGrammar:
                configureJSGFGrammarRecognizer();
                break;
            case NGramModel:
                configureNGramModelRecognizer();
                break;
        }
    }

    /*
     * Configure the Recognizer to utilize the JSGF Grammar
     */
    public synchronized void configureJSGFGrammarRecognizer() {
        new Thread() {
            public void run() {
                // Deallocate current recognizer
                if(liveRecognizer != null){
                    liveRecognizer.deallocate();
                    liveRecognizer = null;
                }
                // Allocate recognizer
                String title = FileUtils.getTextWithoutPath(TextWindow.getInstance().getTitle());
                String text  = FileUtils.getTextAndDigits(TextWindow.getInstance().getText());
                LiveRecognizer recognizer = new LiveRecognizer(title, speechConfiguration,text);
                // Update the grammar name
                updateLanguageModelFileNames();
                // Only allocate the recognizer if the Grammar was built successfully
                if(GrammarUtils.createGrammar(text)){
                    if (recognizer.allocate()) {
                        liveRecognizer = recognizer;
                        // This functionality should not be here but this is the only way to allow for real-time concurrent actions
                        setReadyStateForPlayStopToolbar();
                    }else{ DebuggerWindow.getInstance().addTextLineToPanel("Recognizer : " + title + " cannot be allocated!"); }
                } else{ DebuggerWindow.getInstance().addTextLineToPanel("SpeechManager cannot create grammar for the file [" + title + "]"); }
            }
        }.start();
    }

    /*
     * Configure the Recognizer to utilize the NGramModel
     */
    public synchronized void configureNGramModelRecognizer() {
        new Thread() {
            public void run() {
                // Deallocate current recognizer
                if(liveRecognizer != null){
                    liveRecognizer.deallocate();
                    liveRecognizer = null;
                }
                // Allocate recognizer
                String title = FileUtils.getTextWithoutPath(TextWindow.getInstance().getTitle());
                String text  = FileUtils.getTextAndDigits(TextWindow.getInstance().getText());
                LiveRecognizer recognizer = new LiveRecognizer(title, speechConfiguration,text);
                // Update the Language Model name
                updateLanguageModelFileNames();
                // Only allocate the recognizer if the Grammar was built successfully
                if(StatisticalLanguageModelUtils.createLanguageModel(DIRECTORY,GRAMMAR,text)){
                    if (recognizer.allocate()) {
                        liveRecognizer = recognizer;
                        // This functionality should not be here but this is the only way to allow for real-time concurrent actions
                        setReadyStateForPlayStopToolbar();
                    }else{ DebuggerWindow.getInstance().addTextLineToPanel("Recognizer cannot be allocated!"); }
                } else{ DebuggerWindow.getInstance().addTextLineToPanel("SpeechManager cannot create Language Model for the file [" + title + "]"); }
            }
        }.start();
    }

    private synchronized void setReadyStateForPlayStopToolbar(){
        // Set the toolbar to ready state - keeping concurrent with the rest of the program
        RecognizerActionToolbar.getInstance().setReadyState();
    }

    public synchronized void startRecognizing(){
        if(liveRecognizer == null){
            DebuggerWindow.getInstance().addTextLineToPanel("LiveRecognizer is null");
            return;
        }

        if(liveRecognizer.startRecording()) {
            new AudioHandler(liveRecognizer);
        }
        else if(liveRecognizer.isRecording()){
            DebuggerWindow.getInstance().addTextLineToPanel("Live Recognizer is already recording!!!");
        }
        else{
            DebuggerWindow.getInstance().addTextLineToPanel("Live Recognizer cannot start recording!!!");
        }
    }

    /*
     * Pause the recognition - Store the audio held in the microphone and stop the microphone recognition
     */
    public synchronized void pauseRecognizing(){
        if(liveRecognizer == null){
            DebuggerWindow.getInstance().addTextLineToPanel("LiveRecognizer is null");
            return;
        }
        // Add this utterance into the Utterance list
        liveRecognizer.storeUtteranceToList();
        // Stop the microphone
        liveRecognizer.stopRecording();
    }

    // When we stop the recognizer : Stop recording, reset the reference and take away all highlights from the TextWindow
    public synchronized void stopRecognizing(){
        SpeechManager.resetUserRecognition(liveRecognizer);
    }

    /*
     * Stop recording data from the microphone - set flag to 'Stop Recording' in sphinx
     * Do not clear the utterance list - Utterance list is cleared at 'resetUserRecognition'
     */
    public synchronized void stopRecording(){
        if(liveRecognizer != null){
            // Stop the microphone
            liveRecognizer.stopRecording();
        }
    }

    /*
    * Return true if the microphone is recording - false otherwise
    */
    public synchronized boolean isMicrophoneRecording(){
        boolean isMicrophoneRecording = false;
        // Check if the microphone is recording
        if(liveRecognizer != null){
            isMicrophoneRecording = liveRecognizer.isRecording();
        }
        return isMicrophoneRecording;
    }

    /*
     * Return the microphone audio stream
     */
    @Deprecated
    public synchronized AudioInputStream getMicrophoneAudioStream(){
        AudioInputStream microphoneStream = null;
        if(liveRecognizer != null){
            microphoneStream = liveRecognizer.getAudioStream();
        }
        return microphoneStream;
    }

    /*
    * Return the complete microphone audio stream
    */
    public synchronized AudioInputStream getCompleteMicrophoneAudioStream(){
        AudioInputStream microphoneStream = null;
        if(liveRecognizer != null){
            microphoneStream = liveRecognizer.getCompleteAudioStream();
        }
        return microphoneStream;
    }

    /*
     * Return the name of the live recognizer that is currently in used
     */
    public synchronized String getLiveRecognizerName(){
        String liveRecognizerName = null;
        if(liveRecognizer != null){
            liveRecognizerName = liveRecognizer.getName();
        }
        return liveRecognizerName;
    }

    /*
    * Return the windower property sheet
    * It is helpful for retrieving Audio properties to draw the audio and spectrogram panel
    */
    public synchronized PropertySheet getPropertySheet(){
        PropertySheet propertySheet = null;
        if(liveRecognizer != null){
            propertySheet = liveRecognizer.getPropertySheet();
        }
        return propertySheet;
    }

    /*
    * Return the FrontEnd property
    * It is helpful for retrieving Audio properties to draw the audio and spectrogram panel
    */
    public synchronized FrontEnd getFrontEnd(){
        FrontEnd frontEnd = null;
        if(liveRecognizer != null){
            frontEnd = liveRecognizer.getFrontEnd();
        }
        return frontEnd;
    }

    /*
    * Return the StreamDataSource property
    * It is helpful for retrieving Audio properties to draw the audio and spectrogram panel
    */
    public synchronized StreamDataSource getStreamDataSource(){
        StreamDataSource streamDataSource = null;
        if(liveRecognizer != null){
            streamDataSource = liveRecognizer.getStreamDataSource();
        }
        return streamDataSource;
    }

    public void finalize() throws Throwable{
        // Deallocate the current recognizer
        if(liveRecognizer != null){ liveRecognizer.deallocate(); }
        super.finalize();
    }

    public static synchronized void resetUserRecognition(LiveRecognizer recognizer){
        resetUserRecognition(recognizer,false);
    }

    /*
     * Clear all the recognition process and reset all values used
     * Reset:
     *      - utterance list : List that stores the utterance kept in the microphone
     *      - recognizer     : Stop the microphone recognition
      *     - references     : List of text that the user will read. Each element is a line that the user have to recognize
      *     - highlight      : remove all highlights
      *     - recognizer toolbar : set to ready state the toolbar
     */
    public static synchronized void resetUserRecognition(LiveRecognizer recognizer, boolean disablePlayStopToolbar){
        if(recognizer == null){
            return;
        }
        // Clear the Utterance list
        recognizer.clearUtteranceList();
        // Stop recognizing and reset reference
        recognizer.stopRecording();
        recognizer.resetReference();
        TextWindow.getInstance().removeHighlights();
        // We will reset the toolbar to start from beginning
        if(disablePlayStopToolbar){
            RecognizerActionToolbar.getInstance().setReadyState();
        }
    }

    /*
     * Save the Audio File
     */
    public void saveAudioFile(){
        if(liveRecognizer != null){
            AudioUtils.saveAudioFile(liveRecognizer);
        }
    }
}
