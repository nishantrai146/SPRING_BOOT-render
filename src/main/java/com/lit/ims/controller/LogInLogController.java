package com.lit.ims.controller;

import com.lit.ims.entity.LogInLog;
import com.lit.ims.repository.LogInRepository;
import com.lit.ims.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logging")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LogInLogController {

    private final LogInRepository logInRepository;

    // ✅ Get All Logs
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<LogInLog>>> getAllLogs() {
        List<LogInLog> logs = logInRepository.findAll();
        return ResponseEntity.ok(ApiResponse.<List<LogInLog>>builder()
                .status(true)
                .message("All logs fetched successfully")
                .data(logs)
                .build());
    }

    // ✅ Get Recent Logs (Ordered Descending by Timestamp)
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<LogInLog>>> getRecentLogs() {
        List<LogInLog> logs = logInRepository.findAllByOrderByTimestampDesc();
        return ResponseEntity.ok(ApiResponse.<List<LogInLog>>builder()
                .status(true)
                .message("Recent logs fetched successfully")
                .data(logs)
                .build());
    }
}
