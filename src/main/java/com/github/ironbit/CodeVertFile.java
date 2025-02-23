package com.github.ironbit;

import java.io.File;
import java.util.ArrayList;

/**
 * Abstract class that represents a file that can be converted to another file type.
 */
public abstract class CodeVertFile {
    protected String fileName;
    protected FileExtension fileExtension;
    protected ArrayList<String> keys;
    protected String filePath;

    /**
     * Constructs a CodeVertFile instance from a File object.
     *
     * @param f the File object
     */
    public CodeVertFile(File f) {
        this.fileName = f.getName().substring(0, f.getName().lastIndexOf("."));
        String ext = (f.getName().substring(f.getName().lastIndexOf(".") + 1)).toUpperCase();
        if (FileExtension.contains(ext)) {
            this.fileExtension = FileExtension.valueOf(ext);
        }
        String separator = System.getProperty("os.name").contains("Windows") ? "\\" : "/";
        this.filePath = f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(separator) + 1);
    }

    /**
     * Converts the file to another file extension type.
     *
     * @param fileExtension the target file extension
     * @param selectedKey   the key used for filtering
     * @return the path to the converted file
     */
    abstract String convertTo(FileExtension fileExtension, String selectedKey);


    /**
     * Finds a file name that does not exist in the specified path.
     *
     * @param fileExtension the target file extension
     * @return a unique file name that does not exist in the specified path
     */
    protected String findFileName(FileExtension fileExtension) {
        File f = new File(this.filePath + this.fileName + "." + fileExtension.toString().toLowerCase());
        if (!f.exists()) {
            return f.getAbsolutePath();
        }

        int i = 1;
        while (f.exists()) {
            f = new File(this.filePath + this.fileName + "_" + i + "." + fileExtension.toString().toLowerCase());
            i++;
        }
        return f.getAbsolutePath();
    }

    protected String getFullFilePath() {
        return this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
    }

    public String getFilePath() {
        return filePath;
    }

    void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public ArrayList<String> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<String> keys) {
        this.keys = keys;
    }

    @Override
    public String toString() {
        return "CodeVertFile{" +
                "fileName='" + fileName + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
