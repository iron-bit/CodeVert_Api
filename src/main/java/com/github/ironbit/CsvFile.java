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

    private final char delimiter;

    public CsvFile(File userFile) {
        super(userFile);
        this.keys = new ArrayList<>();
        this.delimiter = detectDelimiter(userFile);
        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader().withColumnSeparator(this.delimiter);

        try {
            MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(new File(this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase()));

            if (it.hasNext()) {
                Map<String, String> firstRow = it.next();
                this.keys.addAll(firstRow.keySet());
            }

            this.keys.forEach(System.out::print);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private char detectDelimiter(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            if (line != null) {
                if (line.contains(";")) {
                    return ';';
                } else if (line.contains(",")) {
                    return ',';
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ';'; // Default to semicolon if no delimiter is detected
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

    private String transformToCsv() {
        return "";
    }

    private String transformToTxt(String selectedKey) {
        String csvFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String txtFilePath = this.findFileName(FileExtension.TXT);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IOException("CSV file is empty or missing header row");
            }

            String[] headers = headerLine.split(delimiter + "");

            MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(new File(csvFilePath));

            StringBuilder txtContent = new StringBuilder();

            while (it.hasNext()) {
                Map<String, String> row = it.next();
                if (row.isEmpty()) continue;

                if (selectedKey == null) {
                    row.forEach((key, value) -> txtContent.append(key).append(": ").append(value).append("\n"));
                } else if (row.containsKey(selectedKey)) {
                    txtContent.append(selectedKey).append(": ").append(row.get(selectedKey)).append("\n");
                }
                txtContent.append("\n");
            }

            if (txtContent.length() == 0) {
                throw new IOException("No data found matching the criteria.");
            }

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

    private String transformToJson(String selectedKey) throws IOException {
        String csvFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String jsonFilePath = this.findFileName(FileExtension.JSON);

        BufferedReader br = new BufferedReader(new FileReader(csvFilePath));
        String line = br.readLine();
        String[] headers = line.split(delimiter + "");

        List<Map<String, String>> data = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            String[] values = line.split(delimiter + "");
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.length; i++) {
                row.put(headers[i].trim(), values[i].trim());
            }
            if (selectedKey == null) {
                data.add(row);
            } else if (row.containsKey(selectedKey)) {
                data.add(Collections.singletonMap(selectedKey, row.get(selectedKey)));
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(jsonFilePath), data);
        br.close();
        return jsonFilePath;
    }

    private String transformToXml(String selectedKey) {
        String csvFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String xmlFilePath = this.findFileName(FileExtension.XML);

        CsvMapper csvMapper = new CsvMapper();
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        ObjectMapper jsonMapper = new ObjectMapper();
        XmlMapper xmlMapper = new XmlMapper();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine();
            if (headerLine == null || headerLine.trim().isEmpty()) {
                throw new IOException("CSV file is empty or missing header row");
            }

            String[] headers = headerLine.split(delimiter + "");

            MappingIterator<Map<String, String>> it = csvMapper.readerFor(Map.class)
                    .with(schema)
                    .readValues(new File(csvFilePath));

            ObjectNode rootNode = jsonMapper.createObjectNode();
            ArrayNode recordsArray = jsonMapper.createArrayNode();

            while (it.hasNext()) {
                Map<String, String> row = it.next();
                if (row.isEmpty()) continue;

                ObjectNode recordNode = jsonMapper.createObjectNode();

                if (selectedKey == null) {
                    // Replace spaces & special chars
                    row.forEach((key, value) -> {
                        String sanitizedKey = key.replaceAll("[^a-zA-Z0-9]", "_");
                        recordNode.put(sanitizedKey, value);
                    });
                } else if (row.containsKey(selectedKey)) {
                    // Process only the selected key
                    String sanitizedKey = selectedKey.replaceAll("[^a-zA-Z0-9]", "_");
                    recordNode.put(sanitizedKey, row.get(selectedKey));
                }

                recordsArray.add(recordNode);
            }

            rootNode.set("record", recordsArray);

            xmlMapper.writeValue(new File(xmlFilePath), rootNode);

            System.out.println("CSV successfully converted to XML and saved as: " + xmlFilePath);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error during conversion: " + e.getMessage());
        }
        return xmlFilePath;
    }
}
