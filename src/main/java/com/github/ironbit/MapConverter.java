package com.github.ironbit;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.*;

class MapConverter {
    private static final String RESULT_PATH = "";

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String convertMap(Map<String, Map<String, String>> map, FileExtension extension, String selectedKey) {
        if (map == null || map.isEmpty()) {
            System.out.println("The input map is empty.");
            return "";
        }

        String resultPath = null;

        if (selectedKey == null) {
            resultPath = convertAllTables(map, extension);
        } else if (map.containsKey(selectedKey)) {
            resultPath = convertTable(map.get(selectedKey), extension, selectedKey);
        } else {
            System.out.println("Key not found: " + selectedKey);
        }
        return resultPath;
    }

    private String convertAllTables(Map<String, Map<String, String>> map, FileExtension extension) {
        return switch (extension) {
            case JSON -> convertAllToJson(map);
            case XML -> convertAllToXml(map);
            case TXT -> convertAllToTxt(map);
            case CSV -> convertAllToCsv(map);
        };
    }

    private String convertTable(Map<String, String> tableData, FileExtension extension, String tableName) {
        return switch (extension) {
            case JSON -> convertToJson(tableData);
            case XML -> convertToXml(tableData);
            case TXT -> convertToTxt(tableData);
            case CSV -> convertToCsv(tableData);
        };
    }

    private String convertAllToJson(Map<String, Map<String, String>> map) {
        try {
            Map<String, List<Map<String, String>>> jsonStructure = new HashMap<>();
            for (var entry : map.entrySet()) {
                jsonStructure.put(entry.getKey(), transformTable(entry.getValue()));
            }
            return saveToFile(FileExtension.JSON, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonStructure));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private String convertToJson(Map<String, String> tableData) {
        try {
            return saveToFile(FileExtension.JSON, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(transformTable(tableData)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
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

    private String convertAllToXml(Map<String, Map<String, String>> map) {
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
        return saveToFile(FileExtension.XML, xml.toString());
    }

    private String convertToXml(Map<String, String> tableData) {
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
        return saveToFile(FileExtension.XML, xml.toString());
    }

    private String convertAllToTxt(Map<String, Map<String, String>> map) {
        String directoryPath = "OutputFiles" + File.separator + "TXT" + File.separator;
        File dir = new File(directoryPath);
        if (!dir.exists()) dir.mkdirs(); // Ensure directory exists

        String filePath = "";
        for (var tableEntry : map.entrySet()) {
            filePath = directoryPath + tableEntry.getKey() + ".txt";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                for (var row : transformTable(tableEntry.getValue())) {
                    row.forEach((key, value) -> {
                        try {
                            writer.write(key + ": " + value + "\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    writer.write("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath; // Return the last file path
    }


    private String convertToTxt(Map<String, String> tableData) {
        StringBuilder txt = new StringBuilder();
        for (var row : transformTable(tableData)) {
            row.forEach((key, value) -> txt.append(key).append(": ").append(value).append("\n"));
            txt.append("\n");
        }
        return saveToFile(FileExtension.TXT, txt.toString());
    }

    private String convertAllToCsv(Map<String, Map<String, String>> map) {
        String directoryPath = "OutputFiles" + File.separator + "CSV" + File.separator;
        File dir = new File(directoryPath);
        if (!dir.exists()) dir.mkdirs(); // Ensure directory exists

        String filePath = "";
        for (var tableEntry : map.entrySet()) {
            filePath = directoryPath + tableEntry.getKey() + ".csv";
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
                writeCsv(writer, tableEntry.getValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return filePath; // Return the last file path
    }


    private String convertToCsv(Map<String, String> tableData) {
        String fileName = findFileName(FileExtension.CSV);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writeCsv(writer, tableData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileName;
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

    private String saveToFile(FileExtension extension, String content) {
        String filename = findFileName(extension);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filename;
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
//        System.out.println(converter.convertMap(mockData, FileExtension.TXT, null));
//
//    }

    private String findFileName(FileExtension fileExtension) {
        File f = new File(MapConverter.RESULT_PATH + "CodevertResult" + "." + fileExtension.toString().toLowerCase());
        if (!f.exists()) {
            return f.getAbsolutePath();
        }

        int i = 1;
        while (f.exists()) {
            f = new File(MapConverter.RESULT_PATH + "CodevertResult" + "_" + i + "." + fileExtension.toString().toLowerCase());
            i++;
        }
        return f.getAbsolutePath();
    }
}
