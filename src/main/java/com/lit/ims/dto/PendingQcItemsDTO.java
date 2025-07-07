package com.lit.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PendingQcItemsDTO {
    private Long id;
    private String itemName;
    private String itemCode;
    private Integer quantity;
    private String batchNumber;
    private String vendorName;
    private String vendorCode;
    private String status;
    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss a")
    private LocalDateTime createdAt;

}
