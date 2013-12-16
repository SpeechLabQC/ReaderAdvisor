package installer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Compress files as a Zip directory
 */
@SuppressWarnings("unused")
public class CompressZipFile {
    // Variables
    private List<String> filesToSkip = new LinkedList<String>();
    private List<String> directoriesToSkip = new LinkedList<String>();

    public CompressZipFile(){
        // Do nothing
    }

    public void addFileToSkip(String fileName){
        this.filesToSkip.add(fileName);
    }

    public void addDirectoryToSkip(String directoryName){
        this.directoriesToSkip.add(directoryName);
    }

    /* *
     * Execute the compressing program
     * @param directoryToZip Directory to zip
     * @return The full name of the file compressed. Null if the zip file was not created.
     */
    public String execute(File directoryToZip) {
       return execute(directoryToZip,null);
    }

    /* *
     * Execute the compressing program
     * @param directoryToZip Directory to zip
     * @param zipName Name of the zip file
     * @return The full name of the file compressed. Null if the zip file was not created.
     */
    public String execute(File directoryToZip, String zipName) {
        String fileName = null;
        if(directoryToZip != null){
            try{
                List<File> fileList = new ArrayList<File>();
                getAllFiles(directoryToZip, fileList);
                fileName = writeZipFile(directoryToZip, fileList, zipName);
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return fileName;
    }

    /*
     * Retrieve all files to be zipped
     */
    private void getAllFiles(File dir, List<File> fileList) throws IOException {
        File[] files = dir.listFiles();
        if(files != null){
            for (File file : files) {
                // Do not add the directories that we need to skip
                if(!isDirectoryToSkip(file.getName())){
                    fileList.add(file);
                    if (file.isDirectory()) {
                        getAllFiles(file, fileList);
                    }
                }
            }
        }
    }

    /*
     * Write the files to a zip
     */
    private String writeZipFile(File directoryToZip, List<File> fileList, String zipName) throws IOException{
        // If the zip name is null then provide the name of the directory
        if(zipName == null){
            zipName = directoryToZip.getName();
        }
        // Store the file name
        String fileName = zipName;
        // Create the zip file
        FileOutputStream fos = new FileOutputStream(fileName);
        ZipOutputStream zos = new ZipOutputStream(fos);
        for (File file : fileList) {
            if (!file.isDirectory()) { // we only zip files, not directories
                // Add files that are not in the skip list
                if(!isFileToSkip(file.getName())) {
                    addToZip(directoryToZip, file, zos);
                }
            }
        }
        zos.close();
        fos.close();
        // Return the full name of the file
        return fileName;
    }

    /*
    * Return true if the directory is to be omitted. Return false otherwise.
    */
    private boolean isDirectoryToSkip(String directoryName){
        if(directoryName != null){
            for(String name : directoriesToSkip){
                // If the file contains the name that we want to omit then return true
                if(directoryName.equals(name)){
                    return true;
                }
            }
        }
        return false;
    }

    /*
    * Return true if the file contains the name that we want to omit. Return false otherwise.
    */
    private boolean isFileToSkip(String fileName){
        if(fileName != null){
            for(String name : filesToSkip){
                // If the file contains the name that we want to omit then return true
                if(fileName.contains(name)){
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * Add files to the zip
     */
    private void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws IOException {
        final int  BUFFER_SIZE = 1024;
        FileInputStream fis = new FileInputStream(file);
        // we want the zipEntry's path to be a relative path that is relative
        // to the directory being zipped, so chop off the rest of the path
        String zipFilePath = file.getCanonicalPath().substring(
                (directoryToZip.getCanonicalPath().length() - directoryToZip.getName().length()),
                file.getCanonicalPath().length());
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);
        byte[] bytes = new byte[BUFFER_SIZE];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }
}
