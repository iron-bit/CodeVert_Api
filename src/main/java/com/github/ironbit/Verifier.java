package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.util.Map.Entry;

import java.util.*;

/**
 * Class that verifies the compatibility of file extensions
 */
class Verifier {
    Map<FileExtension, Set<FileExtension>> map;

    public Verifier() {
        this.map = createMap();
    }

    /**
     * Creates a map with file extensions and their compatibilities.
     *
     * @return a {@code Map<FileExtension, Set<FileExtension>>} with file extensions and their compatibilities.
     * @throws IllegalArgumentException if the JSON resource cannot be parsed.
     */
    private Map<FileExtension, Set<FileExtension>> createMap() throws IllegalArgumentException{
        Map<FileExtension, Set<FileExtension>> map = new HashMap<>();

        File jsonFile = loadJsonResource();

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(jsonFile);
            Iterator<Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> field = fields.next();
                Set<FileExtension> values = new HashSet<>();

                for (JsonNode internalNode : field.getValue()){
                    FileExtension value = FileExtension.valueOf(internalNode.asText().toUpperCase());
                    values.add(value);
                }
                FileExtension key = FileExtension.valueOf(field.getKey().toUpperCase());
                map.put(key, values);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    /**
     * Verifies if the given extension is a valid FileExtension.
     *
     * @param extension the file extension to verify
     * @return the corresponding {@code FileExtension} if valid, otherwise {@code null}
     */
    public FileExtension verifyExtension(String extension){
        if (FileExtension.contains(extension.toUpperCase())) {
            return FileExtension.valueOf(extension.toUpperCase());
        }
        return null;
    }

    /**
     * Verifies if the given extensions are compatible.
     *
     * @param ext1 the first file extension
     * @param ext2 the second file extension
     * @return {@code true} if the extensions are compatible, otherwise {@code false}
     */
    public boolean verifyExtensionCompatibility(FileExtension ext1, FileExtension ext2){
        return map.get(ext1).contains(ext2);
    }

    /**
     * Loads the JSON resource FileTypes.json
     *
     * @return the {@link java.io.File} object representing the JSON resource
     */
    private File loadJsonResource() {
        String resourceName = "FileTypes.json";

        File tempFile;
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            tempFile = Files.createTempFile("resource-", resourceName).toFile();

            FileOutputStream outStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while (true) {
                assert resourceStream != null;
                if ((bytesRead = resourceStream.read(buffer)) == -1) break;
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }
}
