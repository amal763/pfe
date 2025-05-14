package com.jts.login.controller;

import com.jts.login.dto.ApiResponse;
import com.jts.login.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/data")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);
    private final DataService dataService;

    public ApiController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/fetch")
    public ResponseEntity<?> fetchData(
            @RequestParam String apiUrl,
            @RequestParam String tableName) {

        logger.info("Received request to fetch from: {}", apiUrl);

        try {
            // Decode URL in case of any encoding issues
            String decodedUrl = URLDecoder.decode(apiUrl, StandardCharsets.UTF_8.name());
            logger.info("Decoded URL: {}", decodedUrl);

            dataService.fetchAndStoreData(decodedUrl, tableName);
            return ResponseEntity.ok().body(
                    new ApiResponse("success", "Data fetched and stored successfully")
            );

        } catch (Exception e) {
            logger.error("Failed to process request for URL: " + apiUrl, e);
            return ResponseEntity.internalServerError().body(
                    new ApiResponse("error", "Failed to process request: " + e.getMessage())
            );
        }
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllData(@RequestParam String tableName) {
        try {
            logger.info("Fetching all data from table: {}", tableName);

            if (tableName == null || tableName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse("error", "Table name cannot be empty")
                );
            }

            List<Map<String, Object>> data = dataService.getAllData(tableName);

            if (data.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                        new ApiResponse("info", "No data found in table: " + tableName)
                );
            }

            return ResponseEntity.ok(data);

        } catch (Exception e) {
            logger.error("Error in getAllData: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(
                    new ApiResponse("error", "Error retrieving data: " + e.getMessage())
            );
        }
    }

    // Helper method to extract the table name from the API URL
    private String extractTableNameFromUrl(String apiUrl) {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            return null;
        }

        // Assuming the URL format is http://10.222.6.3/CNSS/tablename.php?...
        int startIndex = apiUrl.indexOf("/CNSS/");
        if (startIndex == -1) {
            return null;
        }

        startIndex += "/CNSS/".length();
        int endIndex = apiUrl.indexOf(".php", startIndex);
        if (endIndex == -1) {
            return null;
        }

        return apiUrl.substring(startIndex, endIndex);
    }

    // Global exception handler for the controller
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.internalServerError().body("An unexpected error occurred: " + e.getMessage());
    }
    private static class ApiResponse {
        private String status;
        private String message;

        public ApiResponse(String status, String message) {
            this.status = status;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

    }
}