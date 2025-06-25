package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name="login_log")
public class LogInLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String ipAddress;
    private String branch;
    private String status;
    @Column(name = "timestamp", columnDefinition = "DATETIME(6)", nullable = false)
    private LocalDateTime timestamp;

}
