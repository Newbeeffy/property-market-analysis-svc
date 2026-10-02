package com.interview.market.controller;

import com.interview.market.service.ExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping("/export/csv")
    public ResponseEntity<String> csv() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"properties.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(exportService.csv());
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> pdf() {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"properties.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(exportService.pdf());
    }
}
