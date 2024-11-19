package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.util.Map.Entry;

import java.util.*;

class Verifier {
    Map<String, Set<String>> map;

    public Verifier() {
        this.map = createMap();
    }

    private Map<String, Set<String>> createMap() {
        Map<String, Set<String>> map = new HashMap<>();

        File jsonFile = loadJsonResource();

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode rootNode = mapper.readTree(jsonFile);
            Iterator<Entry<String, JsonNode>> fields = rootNode.fields();
            while (fields.hasNext()) {
                Entry<String, JsonNode> field = fields.next();
                Set<String> set = new HashSet<>();

                for (JsonNode internalNode : field.getValue()){
                    set.add(internalNode.asText().toUpperCase());
                }
                map.put(field.getKey().toUpperCase(), set);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public FileExtension verifyExtension(String extension){
        if (FileExtension.contains(extension.toUpperCase())) {
            return FileExtension.valueOf(extension.toUpperCase());
        }
        return null;
    }

    public boolean verifyExtensionCompatibility(FileExtension ext1, FileExtension ext2){
        //este if en realidad no se necesita, pero por si acaso
        if (map.containsKey(ext1.name())){
            return map.get(ext1.name()).contains(ext2.name());
        }
        return false;
    }

    private File loadJsonResource() {
        String resourceName = "FileTypes.json";

        File tempFile;
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            tempFile = Files.createTempFile("resource-", resourceName).toFile();

            FileOutputStream outStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = resourceStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, bytesRead);
            }
            outStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tempFile;
    }
}
