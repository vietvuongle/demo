package com.example.demo.entity;


import jakarta.persistence.*;
import lombok.*;

import com.example.demo.enums.RoleCode;

@Getter 
@Setter 
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private RoleCode code;

    private String name;
}
