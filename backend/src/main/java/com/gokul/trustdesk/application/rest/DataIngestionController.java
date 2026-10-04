package com.gokul.trustdesk.application.rest;

import com.gokul.trustdesk.application.bootstrap.DataIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/data")
public class DataIngestionController {

    private final DataIngestionService dataIngestionService;

    public DataIngestionController(DataIngestionService dataIngestionService) {
        this.dataIngestionService = dataIngestionService;
    }

    @PostMapping("/load")
    public ResponseEntity<String> loadData() {
        dataIngestionService.ingestSeedData();
        return ResponseEntity.ok("Seed data successfully ingested.");
    }
}