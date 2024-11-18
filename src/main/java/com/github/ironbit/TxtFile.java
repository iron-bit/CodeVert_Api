package com.github.ironbit;

import java.io.File;

class TxtFile extends CodeVertFile {
    public TxtFile() {
        super("TxtFile", "TXT", "Txt content", "Txt path");
    }
    public TxtFile(String fileName, String fileContent, String filePath) {
        super(fileName, "TXT", fileContent, filePath);
    }
    public TxtFile(File userFile) {
        super(userFile);
    }

    @Override
    CodeVertFile convertTo(FileExtension extension) {
        return switch (extension) {
            case JSON -> transformToJson();
            case TXT -> transformToTxt();//No funcionan
            case CSV -> transformToCsv();
            case XML -> transformToXml();
        };
    }

    private CodeVertFile transformToTxt() {
        return new JsonFile();
    }

    private CodeVertFile transformToCsv() {
        return new JsonFile();
    }

    private CodeVertFile transformToXml() {
        return new JsonFile();
    }

    private CodeVertFile transformToJson() {
        return new JsonFile();
    }
}
