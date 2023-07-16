package org.example;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {
    private static int idCounter = 1; // ID counter

    public static void main(String[] args) {
        // Specify the paths of the input CSV files
        String csvFile1 = "college1_data.csv";
        String csvFile2 = "college2_data.csv";
        String csvFile3 = "college3_data.csv";

        // Specify the path of the output CSV file
        String outputFile = "final.csv";

        try {
            // Read the header of each CSV file to determine column orders
            String[] headers1 = readCSVHeader(csvFile1);
            String[] headers2 = readCSVHeader(csvFile2);
            String[] headers3 = readCSVHeader(csvFile3);

            // Extract subject names from the headers
            List<String> subjectNames = extractSubjectNames(headers1);

            // Create a map to store the column order of the final CSV file
            Map<String, Integer> columnOrder = getColumnOrder(subjectNames, headers1, headers2, headers3);

            // Write the header to the final CSV file
            writeCSVHeader(columnOrder, outputFile);

            // Read data from each CSV file and merge it into the final CSV file
            mergeCSVData(csvFile1, columnOrder, outputFile);
            mergeCSVData(csvFile2, columnOrder, outputFile);
            mergeCSVData(csvFile3, columnOrder, outputFile);

            System.out.println("Final CSV file generated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read the header of a CSV file and return it as an array
    private static String[] readCSVHeader(String csvFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line = br.readLine();
        br.close();
        return line.split(",");
    }

    // Extract subject names from the headers
    private static List<String> extractSubjectNames(String[] headers) {
        List<String> subjectNames = new ArrayList<>();
        for (String header : headers) {
            if (!header.toLowerCase().contains("id") && !header.toLowerCase().contains("name")) {
                subjectNames.add(header.trim());
            }
        }
        return subjectNames;
    }

    // Create a map to store the column order of the final CSV file
    private static Map<String, Integer> getColumnOrder(List<String> subjectNames, String[]... headers) {
        Map<String, Integer> columnOrder = new LinkedHashMap<>();
        int columnIndex = 0;

        // Specify the desired column order
        List<String> desiredColumns = new ArrayList<>();
        desiredColumns.add("id");
        desiredColumns.add("college id");
        desiredColumns.add("college name");
        desiredColumns.add("student id");
        desiredColumns.add("student name");
        desiredColumns.addAll(subjectNames);

        // Add the desired columns to the map
        for (String column : desiredColumns) {
            columnOrder.put(column, columnIndex);
            columnIndex++;
        }

        // Add the remaining columns from each header in the order they appear
        for (String[] header : headers) {
            addRemainingColumns(header, columnOrder, columnIndex);
        }

        return columnOrder;
    }

    // Add the remaining columns from a header to the column order map
    private static void addRemainingColumns(String[] headers, Map<String, Integer> columnOrder, int columnIndex) {
        for (String column : headers) {
            if (!columnOrder.containsKey(column)) {
                columnOrder.put(column, columnIndex);
                columnIndex++;
            }
        }
    }

    // Write the header line to the final CSV file
    private static void writeCSVHeader(Map<String, Integer> columnOrder, String outputFile) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));

        // Write the column names in the desired order
        List<String> headerValues = new ArrayList<>(columnOrder.size());
        for (Map.Entry<String, Integer> entry : columnOrder.entrySet()) {
            headerValues.add(entry.getKey());
        }

        // Write the header line to the CSV file
        bw.write(String.join(",", headerValues));
        bw.newLine();

        bw.close();
    }


    // Merge the data from a CSV file into the final CSV file
    private static void mergeCSVData(String csvFile, Map<String, Integer> columnOrder, String outputFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile, true));

        String line;
        boolean isFirstLine = true;
        Map<String, Integer> fileColumnOrder = new LinkedHashMap<>(); // Column order of the current CSV file

        while ((line = br.readLine()) != null) {
            if (isFirstLine) {
                isFirstLine = false;

                // Determine the column order of the current CSV file
                String[] headers = line.split(",");
                int columnIndex = 0;
                for (String header : headers) {
                    fileColumnOrder.put(header, columnIndex);
                    columnIndex++;
                }

                continue; // Skip the header line
            }

            String[] values = line.split(",");

            // Create a map to store the values based on the column names
            Map<String, String> valueMap = new LinkedHashMap<>();

            // Populate the map with values from the current line
            for (Map.Entry<String, Integer> entry : columnOrder.entrySet()) {
                String column = entry.getKey();
                int finalColumnIndex = entry.getValue();

                if (fileColumnOrder.containsKey(column)) {
                    int fileColumnIndex = fileColumnOrder.get(column);
                    if (fileColumnIndex < values.length && values[fileColumnIndex] != null && !values[fileColumnIndex].isEmpty()) {
                        valueMap.put(column, values[fileColumnIndex]);
                    }
                }
            }

            // Create a list to store the ordered values based on the column order
            List<String> orderedValues = new ArrayList<>(columnOrder.size());

            // Increment and add the ID value
            orderedValues.add(Integer.toString(idCounter++));

            // Add the values to the ordered list in the desired order
            for (Map.Entry<String, Integer> entry : columnOrder.entrySet()) {
                String column = entry.getKey();
                if (valueMap.containsKey(column)) {
                    orderedValues.add(valueMap.get(column));
                } else {
                    // Handle specific columns
                    if (!column.equals("id")) {
                        orderedValues.add(""); // Add an empty value for missing column
                    }
                }
            }

            // Write the ordered values to the final CSV file
            bw.write(String.join(",", orderedValues));
            bw.newLine();
        }

        br.close();
        bw.close();
    }
}
