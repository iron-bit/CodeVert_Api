package com.github.ironbit;

import java.io.File;

public class FileConverter {
    private final Verifier verifier;

    public FileConverter() {
        this.verifier = new Verifier();
    }

    public void convert(CodeVertFile file1, FileExtension extension2){
        boolean isCompatible = verifier.verifyExtensionCompatibility(file1.getFileExtension(), extension2);

        if (isCompatible) {
            CodeVertFile file2 = file1.convertTo(extension2);
            file2.saveFile();
            System.out.println("File converted to " + extension2.name() + " successfully.");
            System.out.println("File saved in " + file2.getFilePath());
        } else {
            System.out.println("File extension not compatible.");
        }
    }

    public CodeVertFile prepareFile(File file){
        String strExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase();
        FileExtension extension = verifier.verifyExtension(strExtension);
        return switch (extension) {
            case JSON -> new JsonFile(file);
            case XML -> new XmlFile(file);
            case TXT -> new TxtFile(file);
            default -> null;
        };
    }
}
