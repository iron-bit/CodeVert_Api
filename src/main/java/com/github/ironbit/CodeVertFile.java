package com.github.ironbit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Abstract class that represents a file that can be converted to another file type.
 */
public abstract class CodeVertFile {
    protected String fileName;
    protected FileExtension fileExtension;
    protected String fileContent;
    protected String filePath;

    public CodeVertFile(String fileName, FileExtension fileExtension, String fileContent, String filePath) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileContent = fileContent;
        this.filePath = filePath;
    }

    public CodeVertFile(File f){
        this.fileName = f.getName().substring(0, f.getName().lastIndexOf("."));
        String ext = (f.getName().substring(f.getName().lastIndexOf(".") + 1)).toUpperCase();
        if (FileExtension.contains(ext)) {
            this.fileExtension = FileExtension.valueOf(ext);
        }
        this.filePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("/") + 1);
    }

    /**
     * Converts the file to another file type.
     *
     * @param file The file type to convert to.
     * @return The converted file.
     */
    abstract CodeVertFile convertTo(FileExtension file);

    /**
     * Saves the file in the specified path.
     *
     * @return {@code true} if the file was saved successfully, {@code false} otherwise.
     */
    boolean saveFile() {
        File f = findFileName();
        try (FileWriter writer = new FileWriter(f)){
            writer.write(this.fileContent);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Finds a file name that does not exist in the specified path.
     *
     * @return A {@link java.io.File} with a name that does not exist in the specified path.
     */
    private File findFileName(){
        File f = new File(this.filePath + this.fileName + "." + this.fileExtension);
        if (!f.exists()){
            return f;
        }

        int i = 1;
        while (f.exists()){
            f = new File(this.filePath + this.fileName + "_" + i + "." + this.fileExtension);
            i++;
        }
        return f;
    }

    public String getFilePath() {
        return filePath;
    }
    void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileContent() {
        return fileContent;
    }
    void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }

    public FileExtension getFileExtension() {
        return fileExtension;
    }

    public String getFileName() {
        return fileName;
    }
    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "CodeVertFile{" +
                "fileName='" + fileName + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", fileContent='" + fileContent + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
