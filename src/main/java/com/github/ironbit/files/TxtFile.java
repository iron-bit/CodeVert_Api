package com.github.ironbit.files;

public class TxtFile extends CodeVertFile {
    public TxtFile() {
        super("TxtFile", "TXT", "Txt content", "Txt path");
    }

    public TxtFile(String fileName, String fileContent, String filePath) {
        super(fileName, "TXT", fileContent, filePath);
    }

    @Override
    public CodeVertFile convertTo(FileExtension extension) {
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
