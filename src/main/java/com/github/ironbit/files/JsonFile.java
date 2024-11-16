package com.github.ironbit.files;

public class JsonFile extends CodeVertFile {

    public JsonFile() {
        super("JsonFile", "JSON", "JsonFile content", "JsonFile path");
    }

    @Override
    public CodeVertFile convertTo(FileExtension extension) {
        return switch (extension) {
            case JSON -> transformToJson();
            case XML -> transformToXml();
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
        return new XmlFile();
    }

    private CodeVertFile transformToJson() {
        return new JsonFile();
    }
}
