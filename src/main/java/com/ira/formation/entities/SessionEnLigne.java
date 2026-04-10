package com.ira.formation.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionEnLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;

    private String formateurEmail; // correspond à setFormateurEmail

    private String lienReunion; // correspond à setLienReunion

    @ManyToOne
    @JoinColumn(name = "formation_id")
    private Formation formation;
    
    @Enumerated(EnumType.STRING)
    private SessionStatus statut;
}