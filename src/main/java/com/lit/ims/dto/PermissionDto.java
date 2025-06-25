package com.lit.ims.dto;

import lombok.Data;

@Data
public class PermissionDto {
    private String pageName;
    private boolean canView;
    private boolean canEdit;

}
