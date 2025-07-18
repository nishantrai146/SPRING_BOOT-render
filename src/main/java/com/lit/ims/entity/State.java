package com.lit.ims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true,nullable = false)
    private String iso2; // e.g., MH

    private String name;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;
}
