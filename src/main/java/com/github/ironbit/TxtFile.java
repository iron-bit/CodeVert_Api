package com.github.ironbit;

import java.io.File;

class TxtFile extends CodeVertFile {
    public TxtFile() {
        super("TxtFile", FileExtension.TXT, "Txt content", "Txt path");
    }
    public TxtFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.TXT, fileContent, filePath);
    }
    public TxtFile(File userFile) {
        super(userFile);
    }

    @Override
    CodeVertFile convertTo(FileExtension extension, String selectedKey) {
        return switch (extension) {
            case JSON -> transformToJson();
            case TXT -> transformToTxt();//No funcionan
            case CSV -> transformToCsv();
            case XML -> transformToXml();
            default -> null;
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
