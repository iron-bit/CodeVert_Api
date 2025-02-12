package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

class JsonFile extends CodeVertFile {
    private ArrayList<String> keys;

    public JsonFile() {
        super("JsonFile", FileExtension.JSON, "JsonFile content", "JsonFile path");
    }

    public JsonFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.JSON, fileContent, filePath);
    }

    public JsonFile(File userFile) {
        super(userFile);
        this.keys  = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode rootNode = objectMapper.readTree(new File(this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase()));
            collectKeys(rootNode, keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.keys.forEach(System.out::println);
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
    CodeVertFile convertTo(FileExtension extension, String selectedKey) {
        return switch (extension) {
            case JSON -> transformToJson();
            case XML -> transformToXml(selectedKey);
            case CSV -> transformToCsv(selectedKey);
            case TXT -> transformToTxt();
            default -> null;
        };
    }

    private CodeVertFile transformToTxt() {
        CodeVertFile txtFile;
        try {
            txtFile = new TxtFile();
            txtFile.setFilePath(this.filePath);
            txtFile.setFileName(this.fileName);

            //Ns si hacerlo asi
            BufferedReader reader = new BufferedReader(new FileReader(this.filePath + this.fileName + "." + this.fileExtension));

            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            txtFile.setFileContent(content.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return txtFile;
    }

    private CodeVertFile transformToCsv(String jsonKey) {
        JsonToCsvTransformer jsonToCsvTransformer = new JsonToCsvTransformer(this.filePath, this.fileName, this.fileExtension.toString());
        jsonToCsvTransformer.transformToCsv(jsonKey);
        return new TxtFile();
    }

    private CodeVertFile transformToXml(String jsonKey) {
        String jsonFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String xmlFilePath = this.filePath + this.fileName + "." + FileExtension.XML.toString().toLowerCase();

        System.out.println(xmlFilePath);

        ObjectMapper jsonMapper = new ObjectMapper();   // For reading JSON
        XmlMapper xmlMapper = new XmlMapper();           // For writing XML

        try {
            JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));

            if (jsonKey == null || jsonKey.isEmpty()) {
                xmlMapper.writeValue(new File(xmlFilePath), rootNode);
            } else {
                JsonNode menuItemNode = rootNode.path("menu").path("popup").path(jsonKey);

                if (menuItemNode.isMissingNode()) {
                    xmlMapper.writeValue(new File(xmlFilePath), rootNode);
                } else {
                    ObjectNode rootWrapper = jsonMapper.createObjectNode();
                    rootWrapper.set(jsonKey, menuItemNode);

                    xmlMapper.writeValue(new File(xmlFilePath), rootWrapper);
                }
            }

            System.out.println("JSON successfully converted to XML and saved as: " + xmlFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return new XmlFile();
    }

    private CodeVertFile transformToJson() {
        return new JsonFile();
    }


    public static class JsonToCsvTransformer {

        private String filePath;
        private String fileName;
        private String fileExtension;
        private CodeVertFile parent;

        public JsonToCsvTransformer(String filePath, String fileName, String fileExtension, CodeVertFile parent) {
            this.filePath = filePath;
            this.fileName = fileName;
            this.fileExtension = fileExtension;
            this.parent = parent;
        }

        public CodeVertFile transformToCsv(String jsonKey) {
            String jsonFilePath = parent.getFullFilePath();
            String csvFilePath = parent.findFileName(FileExtension.CSV);

            ObjectMapper jsonMapper = new ObjectMapper();
            CsvMapper csvMapper = new CsvMapper();

            try {
                // Read JSON input from file
                JsonNode rootNode = jsonMapper.readTree(new File(jsonFilePath));

                JsonNode targetNode = extractTargetNode(jsonKey, rootNode);

                if (targetNode.isObject()) {
                    writeObjectToCsv(csvMapper, targetNode, csvFilePath);
                } else if (targetNode.isArray()) {
                    writeArrayToCsv(csvMapper, targetNode, csvFilePath);
                } else {
                    System.err.println("Unsupported JSON structure for CSV conversion.");
                }

                System.out.println("JSON successfully converted to CSV and saved as: " + csvFilePath);

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error during conversion: " + e.getMessage());
            }
            return new TxtFile();
        }

        private JsonNode extractTargetNode(String jsonKey, JsonNode rootNode) {
            if (jsonKey == null || jsonKey.isEmpty()) {
                return rootNode;
            }

            JsonNode targetNode = rootNode.path("menu").path("popup").path(jsonKey);
            return targetNode.isMissingNode() ? rootNode : targetNode;
        }

        private void writeObjectToCsv(CsvMapper csvMapper, JsonNode jsonNode, String csvFilePath) throws IOException {
            CsvSchema.Builder schemaBuilder = CsvSchema.builder();
            Map<String, String> flatJson = flattenJson(jsonNode);

            for (String key : flatJson.keySet()) {
                schemaBuilder.addColumn(key);
            }

            CsvSchema schema = schemaBuilder.build().withHeader().withoutQuoteChar();
            csvMapper.writer(schema).writeValue(new File(csvFilePath), flatJson);
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
            CsvSchema schema = generateSchemaFromFirstObject(jsonArray).withoutQuoteChar();
            csvMapper.writer(schema).writeValue(new File(csvFilePath), jsonArray);
        }

        private CsvSchema generateSchemaFromFirstObject(JsonNode jsonArray) {
            CsvSchema.Builder schemaBuilder = CsvSchema.builder();

            if (jsonArray.size() > 0 && jsonArray.get(0).isObject()) {
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
