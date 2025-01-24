package com.github.ironbit;

import java.io.File;

/**
 * Class responsible for converting files from one type to another.
 * It uses the Verifier class to check compatibility between file extensions.
 */
public class FileConverter {
    private final Verifier verifier;

    /**
     * Constructs a new FileConverter instance and initializes the Verifier.
     */
    public FileConverter() {
        this.verifier = new Verifier();
    }

    /**
     * Converts the given file to the specified extension if they are compatible.
     *
     * @param file1 the file to be converted
     * @param extension2 the target file extension
     */
    public void convert(CodeVertFile file1, FileExtension extension2) {
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

    /**
     * Prepares a CodeVertFile instance from the given file based on its extension.
     *
     * @param file the file to be prepared
     * @return the corresponding CodeVertFile instance, or null if the extension is not supported
     */
    public CodeVertFile prepareFile(File file) {
        String strExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toUpperCase();
        FileExtension extension = verifier.verifyExtension(strExtension);
        return switch (extension) {
            case JSON -> new JsonFile(file);
            case XML -> new XmlFile(file);
            case TXT -> new TxtFile(file);
            default -> throw new CodeVertException("Unsupported file extension.");
        };
    }
}