package com.github.ironbit;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public abstract class CodeVertFile {
    protected String fileName;
    protected String fileExtension;
    protected String fileContent;
    protected String filePath;

    public CodeVertFile(String fileName, String fileExtension, String fileContent, String filePath) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileContent = fileContent;
        this.filePath = filePath;
    }

    public CodeVertFile() {}

    public CodeVertFile(File f){
        this.fileName = f.getName().substring(0, f.getName().lastIndexOf("."));
        this.fileExtension = (f.getName().substring(f.getName().lastIndexOf(".") + 1)).toUpperCase();
        this.filePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf("/") + 1);
    }

    abstract CodeVertFile convertTo(FileExtension file);

    boolean saveFile() {
        File f = new File(this.filePath + this.fileName + "." + this.fileExtension);
//        if (f.exists()){
//            //hacer un bucle para añadir _1 ...
//        }
        try (FileWriter writer = new FileWriter(f)){
            writer.write(this.fileContent);
            return true;
        } catch (IOException e) {
            return false;
        }
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

    public String getFileExtension() {
        return fileExtension;
    }
    void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
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
