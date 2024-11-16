package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ironbit.files.FileExtension;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import java.util.*;

public class Verifier {
    Map<String, Set<String>> map;

    public Verifier() {
        this.map = createMap();
    }

    private Map<String, Set<String>> createMap() {
        Map<String, Set<String>> map = new HashMap<>();
        File jsonFile = new File("src/main/resources/FileTypes.json");
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


}
