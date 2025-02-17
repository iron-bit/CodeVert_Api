package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import java.io.*;
import java.util.*;

class XmlFile extends CodeVertFile {

    public XmlFile() {
        super("XmlFile", FileExtension.XML, "XmlFile path");
    }

    public XmlFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.XML, filePath);
    }

    public XmlFile(File userFile) {
        super(userFile);
        this.keys = new ArrayList<>();
        XmlMapper xmlMapper = new XmlMapper();

        try {
            JsonNode rootNode = xmlMapper.readTree(new File(this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase()));
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
            case XML -> transformToXml();
            case JSON -> transformToJson(selectedKey);
            case CSV -> transformToCsv(selectedKey);
            case TXT -> transformToTxt(selectedKey);
        };
    }

    private String transformToTxt(String selectedKey) {
        String xmlFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String txtFilePath = this.findFileName(FileExtension.TXT);

        XmlMapper xmlMapper = new XmlMapper();

        try {
            JsonNode rootNode = xmlMapper.readTree(new File(xmlFilePath));
            JsonNode targetNode = extractTargetNode(selectedKey, rootNode);

            StringBuilder txtContent = new StringBuilder();
            convertJsonNodeToTxt(targetNode, txtContent, "");

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilePath))) {
                writer.write(txtContent.toString());
            }

            System.out.println("XML successfully converted to TXT and saved as: " + txtFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return txtFilePath;
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

    private String transformToCsv(String xmlKey) {
        XmlToCsvTransformer xmlToCsvTransformer = new XmlToCsvTransformer(this.filePath, this.fileName, this.fileExtension.toString(), this);
        return xmlToCsvTransformer.transformToCsv(xmlKey);
    }

    private String transformToJson(String xmlKey) {
        String xmlFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String jsonFilePath = this.findFileName(FileExtension.JSON);

        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper jsonMapper = new ObjectMapper();

        try {
            JsonNode rootNode = xmlMapper.readTree(new File(xmlFilePath));
            JsonNode targetNode = extractTargetNode(xmlKey, rootNode);

            jsonMapper.writeValue(new File(jsonFilePath), targetNode);

            System.out.println("XML successfully converted to JSON and saved as: " + jsonFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return jsonFilePath;
    }

    private String transformToXml() {
        return "";
    }

    private JsonNode extractTargetNode(String xmlKey, JsonNode rootNode) {
        if (xmlKey == null || xmlKey.isEmpty() || rootNode == null) {
            return rootNode;
        }

        if (rootNode.has(xmlKey)) {
            return rootNode.get(xmlKey);
        }

        JsonNode foundNode = recursiveSearch(xmlKey, rootNode);
        if (foundNode == null) {
            System.err.println("Filtering key not found: " + xmlKey);
        }
        return foundNode;
    }

    private JsonNode recursiveSearch(String xmlKey, JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().equals(xmlKey)) {
                    return field.getValue();
                }
                JsonNode result = recursiveSearch(xmlKey, field.getValue());
                if (result != null) {
                    return result;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayItem : node) {
                JsonNode result = recursiveSearch(xmlKey, arrayItem);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    private static class XmlToCsvTransformer {

        private final String filePath;
        private final String fileName;
        private final String fileExtension;
        private final XmlFile parent;

        public XmlToCsvTransformer(String filePath, String fileName, String fileExtension, XmlFile parent) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.fileExtension = fileExtension;
            this.parent = parent;
        }

        public String transformToCsv(String xmlKey) {
            String xmlFilePath = parent.getFullFilePath();
            String csvFilePath = parent.findFileName(FileExtension.CSV);

            XmlMapper xmlMapper = new XmlMapper();
            CsvMapper csvMapper = new CsvMapper();

            try {
                // Parse XML into JSON tree
                JsonNode rootNode = xmlMapper.readTree(new File(xmlFilePath));

                // Extract relevant node based on key
                JsonNode targetNode = parent.extractTargetNode(xmlKey, rootNode);
                if (targetNode == null) {
                    System.err.println("Key not found in XML: " + xmlKey);
                    return "";
                }

                // Flatten the extracted JSON structure
                Map<String, String> flattenedData = new LinkedHashMap<>();
                flattenJson(targetNode, flattenedData, "", new HashMap<>());

                // Convert to a List (needed for Jackson CSV serialization)
                List<Map<String, String>> csvData = Collections.singletonList(flattenedData);

                // **Fix: Build CSV Schema Without Quotes**
                CsvSchema.Builder schemaBuilder = CsvSchema.builder()
                        .setColumnSeparator(';')   // Set separator to `;`
                        .setUseHeader(true)        // Ensure headers are present
                        .disableQuoteChar();       // **Disable automatic quotes**

                flattenedData.keySet().forEach(schemaBuilder::addColumn);
                CsvSchema schema = schemaBuilder.build();

                // Write CSV
                csvMapper.writer(schema).writeValue(new File(csvFilePath), csvData);

                System.out.println("XML successfully converted to CSV: " + csvFilePath);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error during XML to CSV conversion: " + e.getMessage());
            }
            return csvFilePath;
        }


        private void flattenJson(JsonNode node, Map<String, String> result, String prefix, Map<String, Integer> indexTracker) {
            if (node.isObject()) {
                node.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    String newPrefix = prefix.isEmpty() ? key : prefix + "." + key;
                    flattenJson(entry.getValue(), result, newPrefix, indexTracker);
                });
            } else if (node.isArray()) {
                int index = 0;
                for (JsonNode arrayItem : node) {
                    String arrayKey = prefix + "[" + index + "]";
                    flattenJson(arrayItem, result, arrayKey, indexTracker);
                    index++;
                }
            } else {
                result.put(prefix, node.asText());
            }
        }
    }

}
