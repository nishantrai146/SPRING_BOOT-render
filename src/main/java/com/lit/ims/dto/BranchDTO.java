package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private Long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String email;
    private Long companyId;

    public BranchDTO(Long id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }
}

