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
    void convertTo(FileExtension fileExtension, String selectedKey) {
        switch (fileExtension) {
            case JSON -> transformToJson();
            case TXT -> transformToTxt();//No funcionan
            case CSV -> transformToCsv();
            case XML -> transformToXml();
        }
    }

    private void transformToTxt() {}

    private void transformToCsv() {}

    private void transformToXml() {}

    private void transformToJson() {}
}
