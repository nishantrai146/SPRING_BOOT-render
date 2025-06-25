package com.lit.ims.controller;

import com.lit.ims.entity.LogInLog;
import com.lit.ims.repository.LogInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logging")
public class LogInLogController {
    @Autowired
    private LogInRepository logInRepository;

    @GetMapping
    public List<LogInLog> getLogs(){
        return logInRepository.findAll();
    }
    @GetMapping("/recent")
    public List<LogInLog> getRecentLogs(){
        return logInRepository.findAllByOrderByTimestampDesc();
    }

}
