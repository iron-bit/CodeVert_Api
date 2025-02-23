package com.github.ironbit;

import java.io.File;
import java.util.Map;
import java.util.Set;

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
     * @param file1      the file to be converted
     * @param extension2 the target file extension
     */
    public String convert(CodeVertFile file1, FileExtension extension2, String selectedKey) {
        boolean isCompatible = verifier.verifyExtensionCompatibility(file1.getFileExtension(), extension2);
        String extension = null;
        if (isCompatible) {
            extension = file1.convertTo(extension2, selectedKey);
        } else {
            System.out.println("File extension not compatible.");
        }
        return extension;
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
        System.out.println(extension);
        return switch (extension) {
            case JSON -> new JsonFile(file);
            case XML -> new XmlFile(file);
            case CSV -> new CsvFile(file);
            case TXT -> new TxtFile(file);
            default -> throw new CodeVertException("Unsupported file extension.");
        };
    }

    public Set<FileExtension> getCompatibleExtensions(CodeVertFile file) {
        return verifier.getCompatibilityArray(file);
    }


    public String convertMap(Map<String, Map<String, String>> map, FileExtension extension, String selectedKey) {
        MapConverter converter = new MapConverter();
        return converter.convertMap(map, extension, selectedKey);
    }
}