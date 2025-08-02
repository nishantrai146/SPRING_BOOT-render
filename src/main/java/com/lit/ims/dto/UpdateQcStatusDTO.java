package com.lit.ims.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateQcStatusDTO {
    private Long id;
    private String qcStatus;
    private String defectCategory;
    private String remarks;
    private Long warehouseId;
    private MultipartFile attachment;
}
