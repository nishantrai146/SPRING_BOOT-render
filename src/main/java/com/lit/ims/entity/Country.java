package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
public class Country {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String iso2;

    private String name;
}
