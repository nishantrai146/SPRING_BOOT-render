package com.lit.ims.dto;

import com.lit.ims.entity.Role;
import com.lit.ims.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String branch;
    private String department;
    private Status status;
    private LocalDateTime lastLoginDateTime;
    private String lastLoginIp;
} 