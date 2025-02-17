package com.github.ironbit;

import java.io.File;

class TxtFile extends CodeVertFile {
    public TxtFile() {
        super("TxtFile", FileExtension.TXT, "Txt path");
    }

    public TxtFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.TXT, filePath);
    }

    public TxtFile(File userFile) {
        super(userFile);
    }

    @Override
    String convertTo(FileExtension fileExtension, String selectedKey) {
        return switch (fileExtension) {
            case JSON -> transformToJson();
            case TXT -> transformToTxt();//No funcionan
            case CSV -> transformToCsv();
            case XML -> transformToXml();
        };
    }

    private String transformToTxt() {return "";}

    private String transformToCsv() {return "";}

    private String transformToXml() {return "";}

    private String transformToJson() {return "";}
}
