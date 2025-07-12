package com.lit.ims.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IssuedItemSummaryResponseDTO {

    private String issueNumber;
    private String requisitionNumber;

    @JsonFormat(pattern = "dd/MM/yyyy hh:mm a")
    private LocalDateTime requisitionCreatedAt;

    @JsonFormat(pattern = "dd/MM/yyyy hh:mm a")
    private LocalDateTime issueDate;

    private String type;

    private List<IssuedItemSummaryDTO> items;
}
