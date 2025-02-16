package com.github.ironbit;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

class MapConverter {


    private final ObjectMapper objectMapper = new ObjectMapper(); // Jackson ObjectMapper

    public void convertMap(Map<String, Map<String, String>> map, FileExtension extension, String selectedKey) {
        if (map == null || map.isEmpty()) {
            System.out.println("The input map is empty.");
            return;
        }

        if (selectedKey == null) {
            convertAllTables(map, extension);
        } else if (map.containsKey(selectedKey)) {
            convertTable(map.get(selectedKey), extension, selectedKey);
        } else {
            System.out.println("Key not found: " + selectedKey);
        }
    }

    private void convertAllTables(Map<String, Map<String, String>> map, FileExtension extension) {
        switch (extension) {
            case JSON -> convertAllToJson(map);
            case XML -> convertAllToXml(map);
            case TXT -> convertAllToTxt(map);
            case CSV -> convertAllToCsv(map);
        }
    }

    private void convertTable(Map<String, String> tableData, FileExtension extension, String tableName) {
        switch (extension) {
            case JSON -> convertToJson(tableData, tableName + ".json");
            case XML -> convertToXml(tableData, tableName + ".xml");
            case TXT -> convertToTxt(tableData, tableName + ".txt");
            case CSV -> convertToCsv(tableData, tableName + ".csv");
        }
    }

    private void convertAllToJson(Map<String, Map<String, String>> map) {
        try {
            Map<String, List<Map<String, String>>> jsonStructure = new HashMap<>();
            for (var entry : map.entrySet()) {
                jsonStructure.put(entry.getKey(), transformTable(entry.getValue()));
            }
            saveToFile("data.json", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonStructure));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertToJson(Map<String, String> tableData, String fileName) {
        try {
            saveToFile(fileName, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(transformTable(tableData)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, String>> transformTable(Map<String, String> tableData) {
        List<Map<String, String>> tableList = new ArrayList<>();
        if (!tableData.containsKey("HEADER")) return tableList;

        String[] headers = tableData.get("HEADER").split(",");
        for (var entry : tableData.entrySet()) {
            if (entry.getKey().equals("HEADER")) continue;
            String[] values = entry.getValue().split(",");
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.length && i < values.length; i++) {
                row.put(headers[i].trim(), values[i].trim());
            }
            tableList.add(row);
        }
        return tableList;
    }

    private void convertAllToXml(Map<String, Map<String, String>> map) {
        StringBuilder xml = new StringBuilder("<database>\n");
        for (var tableEntry : map.entrySet()) {
            xml.append("  <table name=\"").append(tableEntry.getKey()).append("\">\n");
            for (var row : transformTable(tableEntry.getValue())) {
                xml.append("    <row>\n");
                for (var field : row.entrySet()) {
                    xml.append("      <").append(field.getKey()).append(">")
                            .append(field.getValue())
                            .append("</").append(field.getKey()).append(">\n");
                }
                xml.append("    </row>\n");
            }
            xml.append("  </table>\n");
        }
        xml.append("</database>");
        saveToFile("data.xml", xml.toString());
    }

    private void convertToXml(Map<String, String> tableData, String fileName) {
        StringBuilder xml = new StringBuilder("<table>\n");
        for (var row : transformTable(tableData)) {
            xml.append("  <row>\n");
            for (var field : row.entrySet()) {
                xml.append("    <").append(field.getKey()).append(">")
                        .append(field.getValue())
                        .append("</").append(field.getKey()).append(">\n");
            }
            xml.append("  </row>\n");
        }
        xml.append("</table>");
        saveToFile(fileName, xml.toString());
    }

    private void convertAllToTxt(Map<String, Map<String, String>> map) {
        StringBuilder txt = new StringBuilder();
        for (var tableEntry : map.entrySet()) {
            txt.append("TABLE: ").append(tableEntry.getKey()).append("\n");
            for (var row : transformTable(tableEntry.getValue())) {
                row.forEach((key, value) -> txt.append(key).append(": ").append(value).append("\n"));
                txt.append("\n");
            }
        }
        saveToFile("data.txt", txt.toString());
    }

    private void convertToTxt(Map<String, String> tableData, String fileName) {
        StringBuilder txt = new StringBuilder();
        for (var row : transformTable(tableData)) {
            row.forEach((key, value) -> txt.append(key).append(": ").append(value).append("\n"));
            txt.append("\n");
        }
        saveToFile(fileName, txt.toString());
    }

    private void convertAllToCsv(Map<String, Map<String, String>> map) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("data.csv"))) {
            for (var tableEntry : map.entrySet()) {
                writer.write("TABLE: " + tableEntry.getKey() + "\n");
                writeCsv(writer, tableEntry.getValue());
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convertToCsv(Map<String, String> tableData, String fileName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writeCsv(writer, tableData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCsv(BufferedWriter writer, Map<String, String> tableData) throws IOException {
        if (!tableData.containsKey("HEADER")) return;
        writer.write(tableData.get("HEADER") + "\n");
        for (var entry : tableData.entrySet()) {
            if (!entry.getKey().equals("HEADER")) {
                writer.write(entry.getValue() + "\n");
            }
        }
    }

    private void saveToFile(String fileName, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
            System.out.println("File saved: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void main(String[] args) {
//        MapConverter converter = new MapConverter();
//
//        Map<String, Map<String, String>> mockData = Map.of(
//                "users", Map.of(
//                        "HEADER", "id,name,email",
//                        "1", "1,John Doe,john@example.com",
//                        "2", "2,Jane Doe,jane@example.com"
//                ),
//                "orders", Map.of(
//                        "HEADER", "order_id,user_id,total",
//                        "1", "101,1,250.00",
//                        "2", "102,2,199.99"
//                )
//        );
//
//        converter.convertMap(mockData, FileExtension.CSV, null);
//
//    }
}
