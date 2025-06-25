package com.lit.ims.service;

import com.lit.ims.entity.LogInLog;
import com.lit.ims.repository.LogInRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LoginLogService {
    @Autowired
    private LogInRepository logInRepository;

    public void log(String username, String ipAddress, String branch, String status) {
        LogInLog log = new LogInLog();
        log.setUsername(username);
        log.setIpAddress(ipAddress);
        log.setBranch(branch);
        log.setStatus(status);
        log.setTimestamp(LocalDateTime.now());
        System.out.println("Saving login log: " + log);


        logInRepository.save(log);
    }
}
