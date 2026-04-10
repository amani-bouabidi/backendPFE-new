package com.ira.formation.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Inscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Utilisateur apprenant;

    @ManyToOne
    private Formation formation;

    private boolean valide;

    private int tentativesTest;

    private double scoreTest;

    // ✅ Nouveau champ pour date du dernier test
    private LocalDateTime dateDernierTest;
}