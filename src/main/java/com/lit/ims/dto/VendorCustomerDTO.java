package com.lit.ims.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VendorCustomerDTO {

    @NotBlank
    private String type;
    @NotBlank
    private String name;
    @NotBlank
    private String mobile;
    @Email
    private String email;
    @NotBlank
    private String address;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    private String pincode;
    @NotBlank
    private String status;
}
