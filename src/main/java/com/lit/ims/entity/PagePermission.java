package com.lit.ims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PagePermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String pageName;
    private boolean canView;
    private boolean canEdit;

    @ManyToOne(fetch= FetchType.LAZY)
    private User user;

}
