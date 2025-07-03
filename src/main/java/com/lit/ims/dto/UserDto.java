package com.lit.ims.dto;

import com.lit.ims.entity.Role;
import com.lit.ims.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private Role role;
    private String department;
    private Status status;
    private String lastLoginIp;
    private LocalDateTime lastLoginDateTime;
    private Long companyId;
    private String companyName;
    private List<BranchInfoDto> branches;
    private List<PermissionDto> permissions;
}
