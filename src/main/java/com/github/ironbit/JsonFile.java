package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.util.*;

class JsonFile extends CodeVertFile {
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
    String convertTo(FileExtension fileExtension, String selectedKey) {
        return switch (fileExtension) {
            case JSON -> transformToJson(selectedKey);
            case XML -> transformToXml(selectedKey);
            case CSV -> transformToCsv(selectedKey);
            case TXT -> transformToTxt(selectedKey);
        };
    }

    private String transformToTxt(String selectedKey) {
        String jsonFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String txtFilePath = this.findFileName(FileExtension.TXT);

        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));
            JsonNode targetNode = extractTargetNode(selectedKey, rootNode);

            StringBuilder txtContent = new StringBuilder();
            if (targetNode.isValueNode()){
                txtContent.append(targetNode.asText()).append("\n");
            }
            else {
                convertJsonNodeToTxt(targetNode, txtContent, "");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilePath))) {
                writer.write(txtContent.toString());
            }

            System.out.println("JSON successfully converted to TXT and saved as: " + txtFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return txtFilePath;
    }

    private void convertJsonNodeToTxt(JsonNode node, StringBuilder txtContent, String indent) {
        if (node.isObject()) {
            node.fields().forEachRemaining(field -> {
                txtContent.append(indent).append(field.getKey()).append(":");
                JsonNode value = field.getValue();
                if (value.isObject() || value.isArray()) {
                    txtContent.append("\n");
                } else {
                    txtContent.append(" ").append(value.asText()).append("\n");
                }
                convertJsonNodeToTxt(value, txtContent, indent + "  ");
            });
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                if (element.isObject() || element.isArray()) {
                    txtContent.append(indent).append("-\n");
                    convertJsonNodeToTxt(element, txtContent, indent + "  ");
                } else {
                    txtContent.append(indent).append("- ").append(element.asText()).append("\n");
                }
            }
        }
    }

    private String transformToCsv(String jsonKey) {
        JsonToCsvTransformer jsonToCsvTransformer = new JsonToCsvTransformer(this.filePath, this.fileName, this.fileExtension.toString(), this);
        return jsonToCsvTransformer.transformToCsv(jsonKey);
    }

    private String transformToXml(String jsonKey) {
        String jsonFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String xmlFilePath = this.findFileName(FileExtension.XML);

        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        try {
            JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));
            JsonNode targetNode = extractTargetNode(jsonKey, rootNode);

            if (targetNode == null) {
                System.err.println("Error: Key '" + jsonKey + "' not found in JSON.");
                return "";
            }

            if (targetNode.isArray()) {
                ObjectNode wrapper = jsonMapper.createObjectNode();
                wrapper.set("record", targetNode);
                targetNode = wrapper;
            }

            // Remove empty fields
            targetNode = removeEmptyFields(targetNode);

            // Write the XML output
            xmlMapper.enable(SerializationFeature.WRAP_ROOT_VALUE); // Helps prevent QName errors
            xmlMapper.writeValue(new File(xmlFilePath), targetNode);

            System.out.println("JSON successfully converted to XML and saved as: " + xmlFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return xmlFilePath;
    }

    /**
     * Removes empty or null fields from JSON before converting to XML.
     */
    private JsonNode removeEmptyFields(JsonNode node) {
        if (node.isObject()) {
            ObjectNode cleanedNode = ((ObjectNode) node).deepCopy();
            Iterator<Map.Entry<String, JsonNode>> fields = cleanedNode.fields();

            List<String> emptyKeys = new ArrayList<>();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                if (entry.getValue().isNull() || (entry.getValue().isTextual() && entry.getValue().asText().isEmpty())) {
                    emptyKeys.add(entry.getKey());
                } else {
                    cleanedNode.set(entry.getKey(), removeEmptyFields(entry.getValue()));
                }
            }

            emptyKeys.forEach(cleanedNode::remove);
            return cleanedNode;
        } else if (node.isArray()) {
            ArrayNode cleanedArray = ((ArrayNode) node).deepCopy();
            for (int i = 0; i < cleanedArray.size(); i++) {
                cleanedArray.set(i, removeEmptyFields(cleanedArray.get(i)));
            }
            return cleanedArray;
        }
        return node;
    }


    private String transformToJson(String jsonKey) {
        String jsonFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String jsonOutputPath = this.findFileName(FileExtension.JSON); // Assuming you want to output JSON file

        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            // Read the JSON from the file
            JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));

            // Extract the node based on the provided key
            JsonNode targetNode = extractTargetNode(jsonKey, rootNode);

            if (targetNode == null) {
                System.err.println("Error: Key '" + jsonKey + "' not found in JSON.");
                return "";
            }

            // If the target node is an array, wrap it in a parent object
            if (targetNode.isArray()) {
                ObjectNode wrapper = jsonMapper.createObjectNode();
                wrapper.set("record", targetNode);
                targetNode = wrapper;
            }

            // Remove empty fields if needed
            targetNode = removeEmptyFields(targetNode);

            // Write the filtered JSON to the output file
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonOutputPath), targetNode);

            System.out.println("Filtered JSON successfully saved as: " + jsonOutputPath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return jsonOutputPath;

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

        public String transformToCsv(String jsonKey) {
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
            return csvFilePath;
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

        private void writeArrayToCsv(CsvMapper csvMapper, JsonNode jsonArray, String csvFilePath) throws IOException {
            List<Map<String, String>> flattenedData = new ArrayList<>();
            for (JsonNode jsonNode : jsonArray) {
                flattenedData.add(flattenJson(jsonNode));
            }

            CsvSchema schema = generateSchemaFromFlattenedData(flattenedData).withoutQuoteChar().withColumnSeparator(';');
            csvMapper.writer(schema).writeValue(new File(csvFilePath), flattenedData);
        }


        private CsvSchema generateSchemaFromFlattenedData(List<Map<String, String>> flattenedData) {
            CsvSchema.Builder schemaBuilder = CsvSchema.builder();
            if (!flattenedData.isEmpty()) {
                for (String key : flattenedData.get(0).keySet()) {
                    schemaBuilder.addColumn(key);
                }
            }
            return schemaBuilder.build().withHeader();
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
    }
}
