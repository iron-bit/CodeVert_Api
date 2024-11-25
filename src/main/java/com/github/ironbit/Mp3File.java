package com.github.ironbit;

import java.io.File;

class Mp3File extends CodeVertFile {
    public Mp3File() {
        super("Mp3File", FileExtension.MP3, "Mp3 content", "Mp3 path");
    }

    public Mp3File(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.MP3, fileContent, filePath);
    }

    public Mp3File(File userFile) {
        super(userFile);
    }

    @Override
    CodeVertFile convertTo(FileExtension extension) {
        return switch (extension) {
            case WAV -> transformToWav();
            default -> null;
        };
    }

    private CodeVertFile transformToWav() {
        return new WavFile();
    }
}
