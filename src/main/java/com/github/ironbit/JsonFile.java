package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.util.*;

class JsonFile extends CodeVertFile {

    public JsonFile() {
        super("JsonFile", FileExtension.JSON, "JsonFile path");
    }

    public JsonFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.JSON, filePath);
    }

    public JsonFile(File userFile) {
        super(userFile);
        this.keys = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(new File(this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase()));
            collectKeys(rootNode, keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        this.keys.forEach(System.out::println);
    }

    private void collectKeys(JsonNode node, ArrayList<String> keys) {
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(key -> {
                keys.add(key);
                collectKeys(node.get(key), keys);
            });
        } else if (node.isArray()) {
            node.forEach(element -> collectKeys(element, keys));
        }
    }


    @Override
    void convertTo(FileExtension fileExtension, String selectedKey) {
        switch (fileExtension) {
            case JSON -> transformToJson();
            case XML -> transformToXml(selectedKey);
            case CSV -> transformToCsv(selectedKey);
            case TXT -> transformToTxt(selectedKey);
        }
    }

    private void transformToTxt(String selectedKey) {
        String jsonFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String txtFilePath = this.findFileName(FileExtension.TXT);

        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));
            JsonNode targetNode = extractTargetNode(selectedKey, rootNode);

            StringBuilder txtContent = new StringBuilder();
            convertJsonNodeToTxt(targetNode, txtContent, "");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilePath))) {
                writer.write(txtContent.toString());
            }

            System.out.println("JSON successfully converted to TXT and saved as: " + txtFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
    }

    private void convertJsonNodeToTxt(JsonNode node, StringBuilder txtContent, String indent) {
        if (node.isObject()) {
            node.fields().forEachRemaining(field -> {
                txtContent.append(indent).append(field.getKey()).append(": ");
                convertJsonNodeToTxt(field.getValue(), txtContent, indent + "  ");
            });
        } else if (node.isArray()) {
            node.forEach(element -> {
                convertJsonNodeToTxt(element, txtContent, indent + "- ");
            });
        } else {
            txtContent.append(node.asText()).append("\n");
        }
    }

    private void transformToCsv(String jsonKey) {
        JsonToCsvTransformer jsonToCsvTransformer = new JsonToCsvTransformer(this.filePath, this.fileName, this.fileExtension.toString(), this);
        jsonToCsvTransformer.transformToCsv(jsonKey);
    }

    private void transformToXml(String jsonKey) {
        String jsonFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String xmlFilePath = this.findFileName(FileExtension.XML);

        System.out.println(xmlFilePath);

        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        try {
            JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));
            JsonNode targetNode = extractTargetNode(jsonKey, rootNode);

            // If the extracted node is NOT an object, wrap it in an object with its key name
            JsonNode finalNode = targetNode;
            if (!targetNode.isObject()) {
                ObjectNode wrapper = jsonMapper.createObjectNode();
                wrapper.set(jsonKey, targetNode);
                finalNode = wrapper;
            }

            xmlMapper.writeValue(new File(xmlFilePath), finalNode);

            System.out.println("JSON successfully converted to XML and saved as: " + xmlFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
    }


    private void transformToJson() {
    }


    private JsonNode extractTargetNode(String jsonKey, JsonNode rootNode) {
        if (jsonKey == null || jsonKey.isEmpty() || rootNode == null) {
            return rootNode;
        }

        // Direct match at the root level
        if (rootNode.has(jsonKey)) {
            return rootNode.get(jsonKey);
        }

        // Recursive search in all fields and arrays
        JsonNode foundNode = recursiveSearch(jsonKey, rootNode);
        if (foundNode == null) {
            System.err.println("Filtering key not found: " + jsonKey);
        }
        return foundNode;
    }

    private JsonNode recursiveSearch(String jsonKey, JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().equals(jsonKey)) {
                    return field.getValue();
                }
                JsonNode result = recursiveSearch(jsonKey, field.getValue());
                if (result != null) {
                    return result;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                JsonNode result = recursiveSearch(jsonKey, arrayItem);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;  // Key not found in this branch
    }

    private static class JsonToCsvTransformer {

        private String filePath;
        private String fileName;
        private String fileExtension;
        private JsonFile parent;

        public JsonToCsvTransformer(String filePath, String fileName, String fileExtension, JsonFile parent) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.fileExtension = fileExtension;
            this.parent = parent;
        }

        public void transformToCsv(String jsonKey) {
            String jsonFilePath = parent.getFullFilePath();
            String csvFilePath = parent.findFileName(FileExtension.CSV);

            ObjectMapper jsonMapper = new ObjectMapper();
            CsvMapper csvMapper = new CsvMapper();

            try {
                // Read JSON input from file
                JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));

                JsonNode targetNode = parent.extractTargetNode(jsonKey, rootNode);

                if (targetNode.isObject()) {
                    writeObjectToCsv(csvMapper, targetNode, csvFilePath);
                } else if (targetNode.isArray()) {
                    writeArrayToCsv(csvMapper, targetNode, csvFilePath);
                } else if (targetNode.isValueNode()) {  // Handle single values (strings, numbers, booleans)
                    writeSingleValueToCsv(csvMapper, jsonKey, targetNode, csvFilePath);
                } else {
                    System.err.println("Unsupported JSON structure for CSV conversion.");
                }

                System.out.println("JSON successfully converted to CSV and saved as: " + csvFilePath);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error during conversion: " + e.getMessage());
            }
        }

        private void writeSingleValueToCsv(CsvMapper csvMapper, String key, JsonNode valueNode, String csvFilePath) throws IOException {
            CsvSchema schema = CsvSchema.builder().addColumn("Key").addColumn("Value").build()
                    .withHeader().withColumnSeparator(';');

            List<Map<String, String>> data = new ArrayList<>();
            Map<String, String> row = new LinkedHashMap<>();
            row.put("Key", key);
            row.put("Value", valueNode.asText());
            data.add(row);

            csvMapper.writer(schema).writeValue(new File(csvFilePath), data);
        }


        private void writeObjectToCsv(CsvMapper csvMapper, JsonNode jsonNode, String csvFilePath) throws IOException {
            CsvSchema.Builder schemaBuilder = CsvSchema.builder();
            Map<String, String> flatJson = flattenJson(jsonNode);

            for (String key : flatJson.keySet()) {
                schemaBuilder.addColumn(key);
            }

            CsvSchema schema = schemaBuilder.build().withHeader().withoutQuoteChar().withColumnSeparator(';');
            csvMapper.writer(schema).writeValue(new File(csvFilePath), Collections.singletonList(flatJson));
        }

        private Map<String, String> flattenJson(JsonNode jsonNode) {
            Map<String, String> flatJson = new LinkedHashMap<>();
            flattenJsonHelper(jsonNode, flatJson, "");
            return flatJson;
        }

        private void flattenJsonHelper(JsonNode jsonNode, Map<String, String> flatJson, String prefix) {
            if (jsonNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> field = fields.next();
                    flattenJsonHelper(field.getValue(), flatJson, prefix.isEmpty() ? field.getKey() : prefix + "." + field.getKey());
                }
            } else if (jsonNode.isArray()) {
                for (int i = 0; i < jsonNode.size(); i++) {
                    flattenJsonHelper(jsonNode.get(i), flatJson, prefix + "[" + i + "]");
                }
            } else {
                flatJson.put(prefix, jsonNode.asText());
            }
        }

        private void writeArrayToCsv(CsvMapper csvMapper, JsonNode jsonArray, String csvFilePath) throws IOException {
            CsvSchema schema = generateSchemaFromFirstObject(jsonArray).withoutQuoteChar().withColumnSeparator(';');
            csvMapper.writer(schema).writeValue(new File(csvFilePath), jsonArray);
        }

        private CsvSchema generateSchemaFromFirstObject(JsonNode jsonArray) {
            CsvSchema.Builder schemaBuilder = CsvSchema.builder();

            if (!jsonArray.isEmpty() && jsonArray.get(0).isObject()) {
                JsonNode firstObject = jsonArray.get(0);
                Iterator<Map.Entry<String, JsonNode>> fields = firstObject.fields();
                while (fields.hasNext()) {
                    schemaBuilder.addColumn(fields.next().getKey());
                }
            }
            return schemaBuilder.build().withHeader();
        }
    }
}
