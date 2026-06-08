package com.sanjoli;

import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class ImportController {

    private final CsvImportService csvService;

    @PostMapping("/import")
    public ResponseEntity<String> importCsv(@RequestParam("filePath") String filePath) {
        try {
            csvService.importCsv(filePath);
            return ResponseEntity.ok("CSV import completed successfully");
        } catch (IOException | CsvException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("CSV import failed: " + e.getMessage());
        }
    }
}


