package com.github.ironbit;

import java.io.File;

class WavFile extends CodeVertFile {
    public WavFile() {
        super("WavFile", FileExtension.WAV, "Mp3 content", "Wav path");
    }

    public WavFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.WAV, fileContent, filePath);
    }

    public WavFile(File userFile) {
        super(userFile);
    }

    @Override
    CodeVertFile convertTo(FileExtension extension) {
        return switch (extension) {
            case MP3 -> transformToWav();
            default -> null;
        };
    }

    private CodeVertFile transformToWav() {
        return new Mp3File();
    }
}
