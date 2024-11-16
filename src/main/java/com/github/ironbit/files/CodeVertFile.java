package com.github.ironbit.files;

public abstract class CodeVertFile {
    String fileName;
    String fileExtension;
    String fileContent;
    String filePath;

    public CodeVertFile(String fileName, String fileExtension, String fileContent, String filePath) {
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileContent = fileContent;
        this.filePath = filePath;
    }
    public CodeVertFile() {}

    public abstract CodeVertFile convertTo(FileExtension file);

    public String getFilePath() {
        return filePath;
    }
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    public String getFileContent() {
        return fileContent;
    }
    public void setFileContent(String fileContent) {
        this.fileContent = fileContent;
    }
    public String getFileExtension() {
        return fileExtension;
    }
    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
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
