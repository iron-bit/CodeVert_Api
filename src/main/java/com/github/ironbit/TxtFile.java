package com.github.ironbit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

class TxtFile extends CodeVertFile {

    public TxtFile(File userFile) {
        super(userFile);
        this.keys = new ArrayList<>();
    }

    @Override
    String convertTo(FileExtension fileExtension, String selectedKey) {
        try {
            return switch (fileExtension) {
                case TXT -> transformToTxt(selectedKey);
                case CSV -> transformToCsv(selectedKey);
                case JSON -> transformToJson(selectedKey);
                case XML -> transformToXml(selectedKey);
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String transformToTxt(String selectedKey) {
        return "";
    }

    private void processLines(BufferedReader br, StringBuilder txtContent, String selectedKey, int indentLevel) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            int currentIndent = countIndentation(line);
            if (currentIndent < indentLevel) return; // Exit if we go back up in indentation

            if (currentIndent == indentLevel) {
                if (line.contains(":")) {
                    // Key-Value Pair
                    String[] parts = line.split(":", 2);
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    if (selectedKey == null || selectedKey.equals(key)) {
                        txtContent.append(key).append(": ").append(value).append("\n");
                    }
                } else if (line.startsWith("-")) {
                    // List Item
                    String listItem = line.substring(1).trim();
                    txtContent.append("- ").append(listItem).append("\n");
                }
                // Recursively handle nested structures
                processLines(br, txtContent, selectedKey, currentIndent + 1);
            }
        }
    }

    private int countIndentation(String line) {
        int indent = 0;
        while (line.startsWith("    ")) { // Assuming 4 spaces per indent level
            indent++;
            line = line.substring(4);
        }
        return indent;
    }

    private String transformToCsv(String selectedKey) throws IOException {
        String txtFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String csvFilePath = this.findFileName(FileExtension.CSV);

        try (BufferedReader br = new BufferedReader(new FileReader(txtFilePath))) {
            List<Map<String, String>> rows = new ArrayList<>();
            Map<String, String> rowData = new HashMap<>();
            Set<String> allKeys = new HashSet<>(); // To capture all unique keys in the file

            String line;
            String currentKey = null;
            List<String> currentList = new ArrayList<>();

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.contains(":")) {
                    // Key-Value Pair
                    String[] parts = line.split(":", 2);
                    currentKey = parts[0].trim();
                    String value = parts[1].trim();

                    // If the current key is a list, append the value to the current list
                    if (currentKey != null && !value.isEmpty()) {
                        if (currentList.size() > 0) {
                            rowData.put(currentKey, String.join(",", currentList));
                            currentList.clear();
                        }
                        rowData.put(currentKey, value);
                    }
                    allKeys.add(currentKey);
                } else if (line.startsWith("-")) {
                    // List Item (part of an array or list)
                    String listItem = line.substring(1).trim();
                    if (currentKey != null && !listItem.isEmpty()) {
                        currentList.add(listItem);
                    }
                }
            }

            // Add the last list data to the rowData if necessary
            if (!currentList.isEmpty() && currentKey != null) {
                rowData.put(currentKey, String.join(",", currentList));
            }

            // Make sure all the keys appear in the CSV (even if some values are missing)
            for (String key : allKeys) {
                if (!rowData.containsKey(key)) {
                    rowData.put(key, "");
                }
            }

            rows.add(rowData); // Add the row to the list of rows

            // Create CSV schema with semicolon delimiter
            CsvMapper csvMapper = new CsvMapper();
            CsvSchema.Builder schemaBuilder = CsvSchema.builder();
            for (String key : allKeys) {
                schemaBuilder.addColumn(key);
            }
            CsvSchema schema = schemaBuilder.build().withHeader().withColumnSeparator(';');

            // Write the data to the CSV file
            csvMapper.writerFor(List.class).with(schema).writeValue(new File(csvFilePath), rows);
            System.out.println("TXT successfully converted to CSV and saved as: " + csvFilePath);
            return csvFilePath;
        }
    }

    private String transformToJson(String selectedKey) throws IOException {
        String txtFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String jsonFilePath = this.findFileName(FileExtension.JSON);

        BufferedReader br = new BufferedReader(new FileReader(txtFilePath));
        String line;
        Map<String, Object> data = new LinkedHashMap<>();
        String currentKey = null;
        List<String> currentList = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                currentKey = parts[0].trim();
                String value = parts[1].trim();

                if (currentList != null && !currentList.isEmpty()) {
                    data.put(currentKey, currentList);
                    currentList = null;
                }

                if (line.endsWith(":")) {
                    currentList = new ArrayList<>();
                } else {
                    data.put(currentKey, value);
                }
            } else if (line.startsWith("-") && currentList != null) {
                String listItem = line.substring(1).trim();
                if (!listItem.isEmpty()) {
                    currentList.add(listItem);
                }
            }
        }

        if (currentList != null && !currentList.isEmpty() && currentKey != null) {
            data.put(currentKey, currentList);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(jsonFilePath), data);
        br.close();

        return jsonFilePath;
    }

    public String transformToXml(String selectedKey) throws IOException, ParserConfigurationException, TransformerException {
        String txtFilePath = this.filePath + this.fileName + "." + this.fileExtension.toString().toLowerCase();
        String xmlFilePath = this.findFileName(FileExtension.XML);

        BufferedReader br = new BufferedReader(new FileReader(txtFilePath));
        String line;
        Map<String, Object> data = new LinkedHashMap<>();
        String currentKey = null;
        List<String> currentList = null;

        // Reading the file and processing the data
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains(":")) {
                String[] parts = line.split(":", 2);
                currentKey = parts[0].trim();
                String value = parts[1].trim();

                if (currentList != null && !currentList.isEmpty()) {
                    data.put(currentKey, currentList);
                    currentList = null;
                }

                if (line.endsWith(":")) {
                    currentList = new ArrayList<>();
                } else {
                    data.put(currentKey, value);
                }
            } else if (line.startsWith("-") && currentList != null) {
                String listItem = line.substring(1).trim();
                if (!listItem.isEmpty()) {
                    currentList.add(listItem);
                }
            }
        }

        if (currentList != null && !currentList.isEmpty() && currentKey != null) {
            data.put(currentKey, currentList);
        }

        // Create XML Document from Map data
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();

        // Root element
        Element rootElement = doc.createElement("root");
        doc.appendChild(rootElement);

        // Recursively add elements from the data map to the XML
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Element keyElement = doc.createElement(entry.getKey());
            if (entry.getValue() instanceof List) {
                for (String item : (List<String>) entry.getValue()) {
                    Element itemElement = doc.createElement("item");
                    itemElement.appendChild(doc.createTextNode(item));
                    keyElement.appendChild(itemElement);
                }
            } else {
                keyElement.appendChild(doc.createTextNode(entry.getValue().toString()));
            }
            rootElement.appendChild(keyElement);
        }

        // Write XML to file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(xmlFilePath));
        transformer.transform(source, result);

        br.close();
        return xmlFilePath;
    }
}
