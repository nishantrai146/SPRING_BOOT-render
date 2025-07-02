package com.lit.ims.dto;

import lombok.Data;
import java.util.List;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String role;
    private String branch;
    private String status;
    private String department;
    private Long companyId;
    private List<PermissionDto> permissions;
    private List<Long> branchIds;
}
