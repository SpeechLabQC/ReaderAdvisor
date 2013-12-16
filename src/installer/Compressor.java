package installer;

import installer.utils.CompressZipFile;
import installer.utils.InstallerUtils;
import installer.utils.ZipUtils;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/*
 * It creates the installer Jar which contains the zipped files of the
 * - Project : for the Developer edition and
 * - ReaderAdvisor    : for the User edition
 */
public class Compressor {
    // Retrieve the properties for this program
    private Properties properties = new Properties();

    // Run the Installer
    public static void run(){
        new Compressor().execute();
    }

    // Do not allow instantiation of this class
    private Compressor(){ }

    // Execute the software
    public void execute(){
        // ---------------- Load the properties ---------------- //
        loadProperties();
        // ---------------- Zip the src directory ---------------- //
        // List to skip from compressing
        CompressZipFile zipCompressor = new CompressZipFile();
        zipCompressor.addFileToSkip(".git"); // GitHub files
        zipCompressor.addFileToSkip(".iml"); // IntelliJ files
        zipCompressor.addFileToSkip(".ipr"); // IntelliJ files
        zipCompressor.addFileToSkip(".iws"); // IntelliJ files
        zipCompressor.addFileToSkip(".zip"); // Zip files
        zipCompressor.addDirectoryToSkip(".git"); // .git directory
        zipCompressor.addDirectoryToSkip("out");  // IntelliJ output directory
        // Select the source directory to be zipped
        File srcDir = InstallerUtils.getFileUsingGui("Choose the Project directory");
        // Zip the bin directory
        String sourceFileName = zipCompressor.execute(srcDir, properties.getProperty("project"));
        // ---------------- Zip the bin directory ---------------- //
        // Select the executable directory to be zipped
        File binDir = InstallerUtils.getFileUsingGui("Choose the Executable directory");
        // Zip the bin directory
        String binFileName = zipCompressor.execute(binDir, properties.getProperty("bin"));
        // ---------------- Move the directories ---------------- //
        // Select the source directory where the zips will be moved
        File target = InstallerUtils.getFileUsingGui("Choose Destination directory");
        // Move the Executable directory
        InstallerUtils.moveDirectory(binFileName, InstallerUtils.getFileFullPath(target));
        // Move the Project directory
        InstallerUtils.moveDirectory(sourceFileName, InstallerUtils.getFileFullPath(target));
        // Exit the program
        System.exit(0);
    }

    // Load the properties of this software
    private void loadProperties(){
        // Load properties message
        try{
            if(InstallerUtils.isRunningFromJar()){
                properties.load(InstallerUtils.classLoader.getResourceAsStream(InstallerUtils.propertiesFile));
            }else{
                try{
                    properties.load(new FileInputStream(InstallerUtils.SCRIPT_DIRECTOR + InstallerUtils.propertiesFile));
                }catch (Exception e){
                    properties.load(new FileInputStream(InstallerUtils.propertiesFile));
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    // Main App
    public static void main(String[] args){
        // Run the installer
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Compressor.run();
            }
        });
    }
}
