package com.github.ironbit;

import java.io.File;

class XmlFile extends CodeVertFile {
    public XmlFile() {
        super("XMLFile", FileExtension.XML, "xml content", "xml path");
    }
    public XmlFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.XML, fileContent, filePath);
    }
    public XmlFile(File userFile) {
        super(userFile);
    }

    @Override
    CodeVertFile convertTo(FileExtension extension, String selectedKey) {
        return switch (extension) {
            case JSON -> transformToJson();
            case XML -> transformToXml();//No funcionan
            case CSV -> transformToCsv();
            case TXT -> transformToTxt();
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
