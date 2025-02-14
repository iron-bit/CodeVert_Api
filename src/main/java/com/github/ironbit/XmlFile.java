package com.github.ironbit;

import java.io.File;

class XmlFile extends CodeVertFile {
    public XmlFile() {
        super("XMLFile", FileExtension.XML, "xml path");
    }

    public XmlFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.XML, filePath);
    }

    public XmlFile(File userFile) {
        super(userFile);
    }

    @Override
    void convertTo(FileExtension fileExtension, String selectedKey) {
        switch (fileExtension) {
            case JSON -> transformToJson();
            case XML -> transformToXml();//No funcionan
            case CSV -> transformToCsv();
            case TXT -> transformToTxt();
        }
    }

    private void transformToTxt() {}

    private void transformToCsv() {}

    private void transformToXml() {}

    private void transformToJson() {}
}
