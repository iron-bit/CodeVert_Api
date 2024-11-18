package com.github.ironbit;

import java.io.*;

class JsonFile extends CodeVertFile {
    public JsonFile() {
        super("JsonFile", "JSON", "JsonFile content", "JsonFile path");
    }
    public JsonFile(String fileName, String fileContent, String filePath) {
        super(fileName, "JSON", fileContent, filePath);
    }
    public JsonFile(File userFile) {
        super(userFile);
    }

    @Override
    CodeVertFile convertTo(FileExtension extension) {
        return switch (extension) {
            case JSON -> transformToJson();
            case XML -> transformToXml();
            case CSV -> transformToCsv();
            case TXT -> transformToTxt();
            default -> null;
        };
    }

    private CodeVertFile transformToTxt() {
        CodeVertFile txtFile;
        try {
            txtFile = new TxtFile();
            txtFile.setFilePath(this.filePath);
            txtFile.setFileName(this.fileName);

            //Ns si hacerlo asi
            BufferedReader reader = new BufferedReader(new FileReader(this.filePath + this.fileName + "." + this.fileExtension));

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            txtFile.setFileContent(content.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return txtFile;
    }

    private CodeVertFile transformToCsv() {
        return new JsonFile();
    }

    private CodeVertFile transformToXml() {
        return new XmlFile();
    }

    private CodeVertFile transformToJson() {
        return new JsonFile();
    }
}
