package com.github.ironbit.files;

public class XmlFile extends CodeVertFile {
    public XmlFile() {
        super("XMLFile", "xml", "xml content", "xml path");
    }

    @Override
    public CodeVertFile convertTo(FileExtension extension) {
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
