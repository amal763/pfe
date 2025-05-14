package com.jts.login.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DataService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DataService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Method to fetch and store data from an external API
    public void fetchAndStoreData(String apiUrl, String tableName) {
        try {
            if (tableName == null || tableName.trim().isEmpty()) {
                throw new IllegalArgumentException("Table name cannot be null or empty");
            }

            // Log the API URL for debugging
            logger.info("Fetching data from API URL: {}", apiUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            logger.info("API Response Status Code: {}", response.getStatusCode());
            logger.info("API Response Headers: {}", response.getHeaders());

            String responseBody = response.getBody();

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Received empty response from API");
            }

            // Log raw response for debugging
            logger.info("Raw API Response Body: {}", responseBody);

            // Remove "200" if it exists at the start
            responseBody = responseBody.trim();
            if (responseBody.startsWith("200")) {
                responseBody = responseBody.substring(3).trim();
            }

            // Ensure response is valid JSON
            if (!responseBody.startsWith("{") && !responseBody.startsWith("[")) {
                throw new RuntimeException("Invalid JSON response received: " + responseBody);
            }

            // Attempt to parse the response
            Map<String, Object> dataMap = objectMapper.readValue(responseBody, Map.class);

            // Extract dataset
            List<List<Object>> dataset = (List<List<Object>>) dataMap.get("dataset");
            if (dataset == null || dataset.isEmpty()) {
                throw new RuntimeException("Dataset is empty or null");
            }

            List<Object> firstRowList = dataset.get(0);
            List<String> columnNames = firstRowList.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());

            // Dynamically create the table with appropriate column types
            createTableIfNotExists(tableName, columnNames, dataset);

            // Prepare SQL statement
            String placeholders = columnNames.stream().map(column -> "?").collect(Collectors.joining(", "));
            String sql = "INSERT INTO " + tableName + " (" + String.join(", ", columnNames) + ") VALUES (" + placeholders + ")";

            // Insert data using batch update (skip header row)
            List<Object[]> batchArgs = dataset.stream().skip(1)
                    .map(row -> row.stream()
                            .map(value -> {
                                if (value == null) {
                                    return null; // Handle null values
                                } else if (value instanceof List || value instanceof Map) {
                                    // Convert lists or maps to JSON strings
                                    try {
                                        return objectMapper.writeValueAsString(value);
                                    } catch (JsonProcessingException e) {
                                        logger.error("Error converting value to JSON: {}", value, e);
                                        return null; // Fallback to null if conversion fails
                                    }
                                } else if (value instanceof byte[]) {
                                    // Handle byte arrays (e.g., serialized objects)
                                    return new String((byte[]) value); // Convert to string
                                } else {
                                    // Convert all other types to strings
                                    return value.toString();
                                }
                            })
                            .toArray())
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(sql, batchArgs);
            logger.info("Data inserted successfully into table: {}", tableName);

        } catch (Exception e) {
            logger.error("Error fetching data: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching data: " + e.getMessage(), e);
        }
    }

    private void createTableIfNotExists(String tableName, List<String> columnNames, List<List<Object>> dataset) {
        try {
            // Generate the SQL to create the table
            StringBuilder columns = new StringBuilder();
            for (int i = 0; i < columnNames.size(); i++) {
                String columnName = columnNames.get(i);
                String columnType = determineColumnType(dataset, i); // Determine the column type dynamically
                columns.append(columnName).append(" ").append(columnType).append(", ");
            }

            // Remove the trailing comma and space
            if (columns.length() > 0) {
                columns.setLength(columns.length() - 2);
            }

            String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + columns.toString() + ")";
            logger.info("Creating table with SQL: {}", sql);

            jdbcTemplate.execute(sql);
            logger.info("Table created successfully: {}", tableName);

        } catch (Exception e) {
            logger.error("Error creating table: {}", e.getMessage(), e);
            throw new RuntimeException("Error creating table: " + e.getMessage(), e);
        }
    }

    private String determineColumnType(List<List<Object>> dataset, int columnIndex) {
        // Analyze the first few rows to determine the column type
        for (List<Object> row : dataset) {
            if (row.size() > columnIndex) {
                Object value = row.get(columnIndex);
                if (value instanceof List || value instanceof Map) {
                    return "TEXT"; // Use TEXT for JSON data
                } else if (value instanceof byte[]) {
                    return "TEXT"; // Use TEXT for binary data
                } else if (value instanceof String) {
                    // If the string is too long, use TEXT instead of VARCHAR
                    if (((String) value).length() > 255) {
                        return "TEXT";
                    }
                }
            }
        }

        // Default to VARCHAR(255) if no specific type is determined
        return "VARCHAR(255)";
    }

    // Method to retrieve all data from a table
    public List<Map<String, Object>> getAllData(String tableName) {
        String sql = "SELECT * FROM " + tableName;
        List<Map<String, Object>> data = jdbcTemplate.queryForList(sql);
        logger.info("Data retrieved from table {}: {}", tableName, data);
        return data;
    }

    // Method to fetch and store ANETI data
    public void fetchAndStoreAnetiData(String dateavantage, String typeavantage) {
        try {
            // Construct the API URL dynamically using UriComponentsBuilder
            String apiUrl = UriComponentsBuilder.fromHttpUrl("http://10.222.6.3/CNSS/beneficiaire_dt.php")
                    .queryParam("date", dateavantage)
                    .queryParam("programme", typeavantage)
                    .toUriString();

            logger.info("Fetching ANETI data from API URL: {}", apiUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);

            logger.info("API Response Status Code: {}", response.getStatusCode());
            logger.info("API Response Headers: {}", response.getHeaders());

            String responseBody = response.getBody();

            if (responseBody == null || responseBody.trim().isEmpty()) {
                throw new RuntimeException("Received empty response from API");
            }

            logger.info("Raw API Response Body: {}", responseBody);

            responseBody = responseBody.trim();
            if (responseBody.startsWith("200")) {
                responseBody = responseBody.substring(3).trim();
            }

            if (!responseBody.startsWith("{") && !responseBody.startsWith("[")) {
                throw new RuntimeException("Invalid JSON response received: " + responseBody);
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode datasetNode = root.path("dataset");

            if (!datasetNode.isArray() || datasetNode.size() == 0) {
                throw new RuntimeException("Dataset is empty or null");
            }

            // Extract column names from the first row
            List<String> columnNames = objectMapper.convertValue(datasetNode.get(0), List.class);
            String placeholders = columnNames.stream().map(column -> "?").collect(Collectors.joining(", "));
            String sql = "INSERT INTO " + typeavantage + " (" + String.join(", ", columnNames) + ") VALUES (" + placeholders + ")";

            // Prepare data for batch insert
            List<Object[]> batchArgs = StreamSupport.stream(datasetNode.spliterator(), false)
                    .skip(1) // Skip header row
                    .map(row -> objectMapper.convertValue(row, List.class).toArray())
                    .collect(Collectors.toList());

            jdbcTemplate.batchUpdate(sql, batchArgs);
            logger.info("Data inserted successfully into table: {}", typeavantage);

        } catch (Exception e) {
            logger.error("Error fetching and storing ANETI data: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching and storing ANETI data: " + e.getMessage(), e);
        }
    }
}