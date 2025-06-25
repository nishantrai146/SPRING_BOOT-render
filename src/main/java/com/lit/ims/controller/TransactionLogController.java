package com.lit.ims.controller;

import com.lit.ims.entity.TransactionLog;
import com.lit.ims.repository.TransactionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class TransactionLogController {
    @Autowired
    private TransactionLogRepository transactionLogRepository;

    @GetMapping
    public List<TransactionLog> getAllLogs() {
        return transactionLogRepository.findAll();
    }

    @GetMapping("/recent")
    public List<TransactionLog> getRecentLogs() {
        return transactionLogRepository.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/recent/top10")
    public List<TransactionLog> getTop10logs() {
        return transactionLogRepository.findTop10ByOrderByTimestampDesc();
    }

}
