package com.optiplant.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "branches")
@Getter
@Setter
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String address;

    private String phone;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean requiresValidation;

    public Branch() {
        this.requiresValidation = true;
    }

    @PrePersist
    public void prePersist() {
        if (requiresValidation == null) {
            requiresValidation = true;
        }
    }
}
