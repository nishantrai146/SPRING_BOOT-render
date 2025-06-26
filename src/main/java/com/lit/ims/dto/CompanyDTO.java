package com.lit.ims.dto;

import lombok.Data;

@Data
public class CompanyDTO {
    private long id;
    private String code;
    private String name;
    private String address;
    private String phone;
    private String email;
}
