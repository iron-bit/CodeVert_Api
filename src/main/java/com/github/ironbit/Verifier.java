package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ironbit.files.FileExtension;

import java.io.*;
import java.nio.file.Files;
import java.util.Map.Entry;

import java.util.*;

public class Verifier {
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

    public FileExtension verifyExtensionCompatibility(String f1Extension, String f2Extension){
        f1Extension = f1Extension.toUpperCase();
        f2Extension = f2Extension.toUpperCase();
        if (map.containsKey(f1Extension)){
            if (map.get(f1Extension).contains(f2Extension)){
                return FileExtension.valueOf(f2Extension);
            }
        }
        return null;
    }
    public boolean verifyExtension(String f1Extension){
        return map.containsKey(f1Extension.toUpperCase());
    }

    private File loadJsonResource() {
        String resourceName = "FileTypes.json";

        File tempFile;
        try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {

//            if (resourceStream == null) {
//                throw new IllegalArgumentException("Resource not found: " + resourceName);
//            }

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
