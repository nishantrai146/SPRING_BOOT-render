package com.lit.ims.service;

import com.lit.ims.entity.TransactionLog;
import com.lit.ims.repository.TransactionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TransactionLogService {

    @Autowired
    private TransactionLogRepository logRepository;

    @Autowired
    private HttpServletRequest request;

    public void log(String action, String entityName, Long entityId, String details) {
        String username = getUsername();
        String ipAddress = getClientIp();

        TransactionLog log = TransactionLog.builder()
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .performedBy(username)
                .ipAddress(ipAddress)
                .details(details)
                .timestamp(LocalDateTime.now())
                .build();

        logRepository.save(log);
    }

    private String getUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "anonymous";
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}