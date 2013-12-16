package readerAdvisor;

import readerAdvisor.environment.EnvironmentUtils;
import readerAdvisor.gui.TextWindow;

import javax.swing.*;

//TODO: Create a Logger class - http://www.vogella.com/articles/Logging/article.html
public class MainApp {
    public static final String SOFTWARE_VERSION = "1.0";
    // To run the class from the command line or from an IDE
    // VM Options : -server -XX:+PrintGCDetails -Dfrontend=epFrontEnd -Dmicrophone[keepLastAudio]=true -DconfigurationFile=script/software.properties
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                try{
                    // Initiate the GUI Environment Settings
                    EnvironmentUtils.setUpSoftwareEnvironment();
                    // Start the program
                    TextWindow.getInstance().startGUI();
                }catch(Exception e){
                    e.printStackTrace( );
                }
            }
        });
    }
}



