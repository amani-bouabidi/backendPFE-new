package com.ira.formation.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
	    uniqueConstraints = @UniqueConstraint(columnNames = {"titre", "formateur_id"})
	)
public class Formation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    private String description;

    // formation appartient à un formateur
    @ManyToOne
    @JoinColumn(name = "formateur_id")
    private Utilisateur formateur;

    @OneToMany(mappedBy = "formation", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Module> modules;
    
    @ManyToOne
    @JoinColumn(name = "domaine_id", nullable = true)
    private Domaine domaine;
}