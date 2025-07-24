package com.lit.ims.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupedItemGroupDTO {
    private String type;
    private String parentBomCode;
    private String parentBomName;
    private List<GroupedItemDTO> items;
}
