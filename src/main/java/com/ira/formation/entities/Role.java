package com.ira.formation.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "Le nom du rôle est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom du rôle doit contenir entre 3 et 50 caractères")
    private String nom;

    // Optionnel : si tu veux plus tard ajouter une description
    // private String description;
}