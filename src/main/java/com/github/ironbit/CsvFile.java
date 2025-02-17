package com.github.ironbit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.*;
import java.util.*;

class CsvFile extends CodeVertFile {

    public CsvFile(String fileName, String fileContent, String filePath) {
        super(fileName, FileExtension.CSV, filePath);
    }

    public CsvFile(File userFile) {
        super(userFile);
        this.keys = new ArrayList<>();
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        try {
            MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(new File(this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase()));

            if (it.hasNext()) {
                Map<String, String> firstRow = it.next();
                this.keys.addAll(firstRow.keySet());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    String convertTo(FileExtension fileExtension, String selectedKey) {
        try {
            return switch (fileExtension) {
                case CSV -> transformToCsv();
                case JSON -> transformToJson(selectedKey);
                case XML -> transformToXml(selectedKey);
                case TXT -> transformToTxt(selectedKey);
            };
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String transformToTxt(String selectedKey) {
        String csvFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String txtFilePath = this.findFileName(FileExtension.TXT);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader(); // Ensure headers are included

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IOException("CSV file is empty or missing header row");
            }

            // MappingIterator to read all rows
            MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(new File(csvFilePath));

            // Prepare the StringBuilder for text output
            StringBuilder txtContent = new StringBuilder();

            // Read and track the row index (1-based) and transform based on selectedKey
            int currentIndex = 1; // Starting from 1 to match the selectedKey index

            while (it.hasNext()) {
                Map<String, String> row = it.next();
                if (row.isEmpty()) continue;  // Skip empty rows

                // Check if we should process this row based on selectedKey
                if (selectedKey == null || Integer.parseInt(selectedKey) == currentIndex) {
                    row.forEach((key, value) -> {
                        // Write each key-value pair to the text output
                        txtContent.append(key).append(": ").append(value).append("\n");
                    });
                    // Add a separator between rows (optional, you can customize the separator)
                    txtContent.append("\n");
                }

                currentIndex++; // Increment index for next row
            }

            if (txtContent.length() == 0) {
                throw new IOException("No data found matching the criteria.");
            }

            // Write the generated text content to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(txtFilePath))) {
                writer.write(txtContent.toString());
            }

            System.out.println("CSV successfully converted to TXT and saved as: " + txtFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return txtFilePath;
    }


    private String transformToCsv() {
        return "";
    }

    private String transformToJson(String selectedKey) throws IOException {
        String csvFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String jsonFilePath = this.findFileName(FileExtension.JSON);

        BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
        String line = br.readLine(); // Read the header row
        String[] headers = line.split(","); // Change delimiter to comma

        List<Map<String, String>> data = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.length; i++) {
                row.put(headers[i].trim(), values[i].trim());
            }
            if (selectedKey == null || row.get(headers[0]).equals(selectedKey)) {
                data.add(row);
            }
        }

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Convert list of maps to JSON
        ArrayNode arrayNode = objectMapper.createArrayNode();
        for (Map<String, String> row : data) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            row.forEach(objectNode::put);
            arrayNode.add(objectNode);
        }

        objectMapper.writeValue(new File(jsonFilePath), arrayNode);
        br.close();
        return jsonFilePath;
    }

    private String transformToXml(String selectedKey) {
        String csvFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String xmlFilePath = this.findFileName(FileExtension.XML);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader(); // Ensure headers are included
        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IOException("CSV file is empty or missing header row");
            }

            // MappingIterator to read all rows
            MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(new File(csvFilePath));

            // Create the ArrayNode to hold all rows for XML conversion
            ArrayNode arrayNode = jsonMapper.createArrayNode();

            // Read and track the row index (1-based) and transform based on selectedKey
            int currentIndex = 1;  // Starting from 1 to match the selectedKey index

            while (it.hasNext()) {
                Map<String, String> row = it.next();
                if (row.isEmpty()) continue;  // Skip empty rows

                // Check if we should process this row based on selectedKey
                if (selectedKey == null || Integer.parseInt(selectedKey) == currentIndex) {
                    ObjectNode objectNode = jsonMapper.createObjectNode();
                    row.forEach((key, value) -> {
                        String sanitizedKey = key.replaceAll("[^a-zA-Z0-9]", "_"); // Sanitize keys
                        objectNode.put(sanitizedKey, value);
                    });
                    arrayNode.add(objectNode);
                }

                currentIndex++; // Increment index for next row
            }

            if (arrayNode.size() == 0) {
                throw new IOException("No data found matching the criteria.");
            }

            // Root node for XML
            ObjectNode rootNode = jsonMapper.createObjectNode();
            rootNode.set("root", arrayNode);

            // Convert to XML
            xmlMapper.writeValue(new File(xmlFilePath), rootNode);

            System.out.println("CSV successfully converted to XML and saved as: " + xmlFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return xmlFilePath;
    }



}
