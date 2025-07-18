package com.lit.ims.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StateDTO {
    private Long id;
    private String name;
    private String iso2;
}
