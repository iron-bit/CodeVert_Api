package com.github.ironbit;

import com.github.ironbit.files.CodeVertFile;
import com.github.ironbit.files.FileExtension;

public class FileConverter {
    public FileConverter(CodeVertFile file1, String file2StringExtension) {
        String extension = file1.getFileExtension().toUpperCase();

        Verifier verifier = new Verifier();
        FileExtension file2Extension = verifier.verifyExtensionCompatibility(extension, file2StringExtension);
        if (file2Extension != null) {
            CodeVertFile file2 = file1.convertTo(file2Extension);
            System.out.println("File converted to " + file2Extension.name() + " successfully.");

            System.out.println("File 1: " + file1);
            System.out.println("File 2: " + file2);
        } else {
            System.out.println("File extension not compatible.");
        }
    }
}
